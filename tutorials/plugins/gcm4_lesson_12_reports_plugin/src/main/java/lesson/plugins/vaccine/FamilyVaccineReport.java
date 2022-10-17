package lesson.plugins.vaccine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lesson.plugins.family.FamilyAdditionEvent;
import lesson.plugins.family.FamilyDataManager;
import lesson.plugins.family.FamilyId;
import lesson.plugins.family.FamilyMemberShipAdditionEvent;
import lesson.plugins.person.PersonAdditionEvent;
import lesson.plugins.person.PersonDataManager;
import lesson.plugins.person.PersonId;
import nucleus.ActorContext;
import nucleus.EventFilter;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import util.wrappers.MutableInteger;

public class FamilyVaccineReport {

	private static enum FamilyVaccineStatus {
		NONE("unvacinated_families"), //
		PARTIAL("partially_vaccinated_families"), //
		FULL("fully_vaccinated_families");//

		private final String description;

		private FamilyVaccineStatus(final String description) {
			this.description = description;
		}
	}

	private static enum IndividualVaccineStatus {
		NONE("unvaccinated_individuals"), //
		FULL("vaccinated_individuals");//

		private final String description;

		private IndividualVaccineStatus(final String description) {
			this.description = description;
		}
	}

	private final ReportId reportId;
	
	private ReportHeader reportHeader;

	private ActorContext actorContext;

	private VaccinationDataManager vaccinationDataManager;

	private FamilyDataManager familyDataManager;
	
	private final Map<FamilyVaccineStatus, MutableInteger> statusToFamiliesMap = new LinkedHashMap<>();

	private final Map<FamilyId, FamilyVaccineStatus> familyToStatusMap = new LinkedHashMap<>();
	
	private final Map<IndividualVaccineStatus, MutableInteger> statusToIndividualsMap = new LinkedHashMap<>();

	private final Map<PersonId, IndividualVaccineStatus> individualToStatusMap = new LinkedHashMap<>();

	

	public FamilyVaccineReport(final ReportId reportId) {
		this.reportId = reportId;
		
		final ReportHeader.Builder builder = ReportHeader.builder();
		builder.add("time");
		for (final FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
			builder.add(familyVaccineStatus.description);
		}
		for (final IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
			builder.add(individualVaccineStatus.description);
		}
		reportHeader = builder.build();
	}

	

	private void handleFamilyAdditionEvent(final ActorContext actorContext, final FamilyAdditionEvent familyAdditionEvent) {
		refreshFamilyStatus(familyAdditionEvent.getFamilyId());
	}

	private void handleFamilyMemberShipAdditionEvent(final ActorContext actorContext, final FamilyMemberShipAdditionEvent familyMemberShipAdditionEvent) {
		individualToStatusMap.remove(familyMemberShipAdditionEvent.getPersonId());
		refreshFamilyStatus(familyMemberShipAdditionEvent.getFamilyId());
	}

	private void handlePersonAdditionEvent(final ActorContext actorContext, final PersonAdditionEvent personAdditionEvent) {
		final PersonId personId = personAdditionEvent.getPersonId();
		final Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			final FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}

	private void handleVaccinationEvent(final ActorContext actorContext, final VaccinationEvent vaccinationEvent) {
		final PersonId personId = vaccinationEvent.getPersonId();

		final Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);

		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			final FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}

	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;
		/*
		 * Subscribe to all the relevant events
		 */		
		actorContext.subscribe(EventFilter.builder(VaccinationEvent.class).build(), this::handleVaccinationEvent);
		actorContext.subscribe(EventFilter.builder(FamilyAdditionEvent.class).build(), this::handleFamilyAdditionEvent);
		actorContext.subscribe(EventFilter.builder(FamilyMemberShipAdditionEvent.class).build(), this::handleFamilyMemberShipAdditionEvent);
		actorContext.subscribe(EventFilter.builder(PersonAdditionEvent.class).build(), this::handlePersonAdditionEvent);

		/*
		 * Some of the events may have already occurred before we initialize
		 * this report, so we will need to build up out status maps
		 */

		familyDataManager = actorContext.getDataManager(FamilyDataManager.class);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);

		for (final FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
			statusToFamiliesMap.put(familyVaccineStatus, new MutableInteger());
		}

		for (final IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
			statusToIndividualsMap.put(individualVaccineStatus, new MutableInteger());
		}

		// determine the family vaccine status for every family
		
		for (final FamilyId familyId : familyDataManager.getFamilyIds()) {
			
			final int familySize = familyDataManager.getFamilySize(familyId);
			final List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
			int vaccinatedCount = 0;
			for (final PersonId personId : familyMembers) {
				if (vaccinationDataManager.isPersonVaccinated(personId)) {
					vaccinatedCount++;
				}
			}
			FamilyVaccineStatus status;

			if (vaccinatedCount == 0) {
				status = FamilyVaccineStatus.NONE;
			} else if (vaccinatedCount == familySize) {
				status = FamilyVaccineStatus.FULL;
			} else {
				status = FamilyVaccineStatus.PARTIAL;
			}
			
			statusToFamiliesMap.get(status).increment();
			familyToStatusMap.put(familyId, status);
			
		}

		// ensure that any person not assigned to a family is still counted
		
		
		for (final PersonId personId : personDataManager.getPeople()) {
			if (familyDataManager.getFamilyId(personId).isEmpty()) {
				
				IndividualVaccineStatus status;
				if (vaccinationDataManager.isPersonVaccinated(personId)) {
					status = IndividualVaccineStatus.FULL;
				} else {
					status = IndividualVaccineStatus.NONE;
				}				
				statusToIndividualsMap.get(status).increment();
				individualToStatusMap.put(personId, status);
			}
		}
		
		releaseReportItem();
	}

	private void refreshFamilyStatus(final FamilyId familyId) {

		final int familySize = familyDataManager.getFamilySize(familyId);
		final List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
		int vaccinatedCount = 0;
		for (final PersonId personId : familyMembers) {
			if (vaccinationDataManager.isPersonVaccinated(personId)) {
				vaccinatedCount++;
			}
		}
		FamilyVaccineStatus newStatus;

		if (vaccinatedCount == 0) {
			newStatus = FamilyVaccineStatus.NONE;
		} else if (vaccinatedCount == familySize) {
			newStatus = FamilyVaccineStatus.FULL;
		} else {
			newStatus = FamilyVaccineStatus.PARTIAL;
		}

		final FamilyVaccineStatus currentStatus = familyToStatusMap.get(familyId);
		if (currentStatus == newStatus) {
			return;
		}
		if (currentStatus != null) {
			statusToFamiliesMap.get(currentStatus).decrement();
		}
		statusToFamiliesMap.get(newStatus).increment();
		familyToStatusMap.put(familyId, newStatus);
		releaseReportItem();
	}

	private void refreshIndividualStatus(final PersonId personId) {
		IndividualVaccineStatus newStatus;
		if (vaccinationDataManager.isPersonVaccinated(personId)) {
			newStatus = IndividualVaccineStatus.FULL;
		} else {
			newStatus = IndividualVaccineStatus.NONE;
		}

		final IndividualVaccineStatus currentStatus = individualToStatusMap.get(personId);

		if (currentStatus == newStatus) {
			return;
		}

		if (currentStatus != null) {
			statusToIndividualsMap.get(currentStatus).decrement();
		}
		statusToIndividualsMap.get(newStatus).increment();
		individualToStatusMap.put(personId, newStatus);
		releaseReportItem();
	}

	private void releaseReportItem() {
		final ReportItem.Builder builder = ReportItem.builder().setReportId(reportId).setReportHeader(reportHeader);
		builder.addValue(actorContext.getTime());
		for (final FamilyVaccineStatus familyVaccineStatus : statusToFamiliesMap.keySet()) {
			MutableInteger mutableInteger = statusToFamiliesMap.get(familyVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		for (final IndividualVaccineStatus individualVaccineStatus : statusToIndividualsMap.keySet()) {
			MutableInteger mutableInteger = statusToIndividualsMap.get(individualVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		final ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);
	}
}
