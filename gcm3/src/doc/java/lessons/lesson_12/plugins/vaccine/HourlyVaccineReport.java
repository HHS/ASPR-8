package lessons.lesson_12.plugins.vaccine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lessons.lesson_12.plugins.family.FamilyAdditionEvent;
import lessons.lesson_12.plugins.family.FamilyDataManager;
import lessons.lesson_12.plugins.family.FamilyId;
import lessons.lesson_12.plugins.family.FamilyMemberShipAdditionEvent;
import lessons.lesson_12.plugins.person.PersonAdditionEvent;
import lessons.lesson_12.plugins.person.PersonDataManager;
import lessons.lesson_12.plugins.person.PersonId;
import nucleus.ActorContext;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.wrappers.MutableInteger;

public class HourlyVaccineReport extends PeriodicReport {

	public HourlyVaccineReport(ReportId reportId, ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);	
	}

	private VaccinationDataManager vaccinationDataManager;

	private FamilyDataManager familyDataManager;

	private Map<FamilyVaccineStatus, MutableInteger> statusToFamiliesMap = new LinkedHashMap<>();
	private Map<FamilyId, FamilyVaccineStatus> familyToStatusMap = new LinkedHashMap<>();

	private Map<IndividualVaccineStatus, MutableInteger> statusToIndividualsMap = new LinkedHashMap<>();
	private Map<PersonId, IndividualVaccineStatus> individualToStatusMap = new LinkedHashMap<>();

	private static enum FamilyVaccineStatus {
		NONE("unvacinated_families"), PARTIAL("partially_vaccinated_families"), FULL("fully_vaccinated_families");

		private final String description;

		private FamilyVaccineStatus(String description) {
			this.description = description;
		}
	}

	private static enum IndividualVaccineStatus {
		NONE("unvaccinated_individuals"), FULL("vaccinated_individuals");

		private final String description;

		private IndividualVaccineStatus(String description) {
			this.description = description;
		}

	}

	private void refreshFamilyStatus(FamilyId familyId) {

		int familySize = familyDataManager.getFamilySize(familyId);
		List<PersonId> familyMembers = familyDataManager.getFamilyMembers(familyId);
		int vaccinatedCount = 0;
		for (PersonId personId : familyMembers) {
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

		FamilyVaccineStatus currentStatus = familyToStatusMap.get(familyId);
		if (currentStatus == newStatus) {
			return;
		}
		if (currentStatus != null) {
			statusToFamiliesMap.get(currentStatus).decrement();
		}
		statusToFamiliesMap.get(newStatus).increment();
		familyToStatusMap.put(familyId, newStatus);

	}

	private void refreshIndividualStatus(PersonId personId) {
		IndividualVaccineStatus newStatus;
		if (vaccinationDataManager.isPersonVaccinated(personId)) {
			newStatus = IndividualVaccineStatus.FULL;
		} else {
			newStatus = IndividualVaccineStatus.NONE;
		}

		IndividualVaccineStatus currentStatus = individualToStatusMap.get(personId);

		if (currentStatus == newStatus) {
			return;
		}

		if (currentStatus != null) {
			statusToIndividualsMap.get(currentStatus).decrement();
		}
		statusToIndividualsMap.get(newStatus).increment();
		individualToStatusMap.put(personId, newStatus);

	}

	public void init(ActorContext actorContext) {
		super.init(actorContext);

		/*
		 * Subscribe to all the relevant events
		 */
		
		actorContext.subscribe(VaccinationEvent.class, getFlushingConsumer(this::handleVaccinationEvent));
		actorContext.subscribe(FamilyAdditionEvent.class, getFlushingConsumer(this::handleFamilyAdditionEvent));
		actorContext.subscribe(FamilyMemberShipAdditionEvent.class, getFlushingConsumer(this::handleFamilyMemberShipAdditionEvent));
		actorContext.subscribe(PersonAdditionEvent.class, getFlushingConsumer(this::handlePersonAdditionEvent));

		/*
		 * Some of the events may have already occurred before we initialize
		 * this report, so we will need to build up out status maps
		 */

		familyDataManager = actorContext.getDataManager(FamilyDataManager.class);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);

		for (FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
			statusToFamiliesMap.put(familyVaccineStatus, new MutableInteger());
		}

		for (IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
			statusToIndividualsMap.put(individualVaccineStatus, new MutableInteger());
		}

		// determine the family vaccine status for every family
		for (FamilyId familyId : familyDataManager.getFamilyIds()) {
			refreshFamilyStatus(familyId);
		}

		// ensure that any person not assigned to a family is still counted
		for (PersonId personId : personDataManager.getPeople()) {
			if (familyDataManager.getFamilyId(personId).isEmpty()) {
				refreshIndividualStatus(personId);
			}
		}
	}

	private void handlePersonAdditionEvent(ActorContext actorContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}

	}

	private void handleVaccinationEvent(ActorContext actorContext, VaccinationEvent vaccinationEvent) {
		PersonId personId = vaccinationEvent.getPersonId();

		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);

		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}

	private void handleFamilyAdditionEvent(ActorContext actorContext, FamilyAdditionEvent familyAdditionEvent) {
		refreshFamilyStatus(familyAdditionEvent.getFamilyId());
	}

	private void handleFamilyMemberShipAdditionEvent(ActorContext actorContext, FamilyMemberShipAdditionEvent familyMemberShipAdditionEvent) {
		individualToStatusMap.remove(familyMemberShipAdditionEvent.getPersonId());
		refreshFamilyStatus(familyMemberShipAdditionEvent.getFamilyId());
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder builder = ReportHeader.builder();
			addTimeFieldHeaders(builder);			
			for (FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
				builder.add(familyVaccineStatus.description);
			}
			for (IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
				builder.add(individualVaccineStatus.description);
			}
			reportHeader = builder.build();
		}
		return reportHeader;
	}

	@Override
	protected void flush(ActorContext actorContext) {
		ReportItem.Builder builder = ReportItem.builder()//
				.setReportId(getReportId())//
				.setReportHeader(getReportHeader());
		fillTimeFields(builder);		
		for (FamilyVaccineStatus familyVaccineStatus : statusToFamiliesMap.keySet()) {
			MutableInteger mutableInteger = statusToFamiliesMap.get(familyVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		for (IndividualVaccineStatus individualVaccineStatus : statusToIndividualsMap.keySet()) {
			MutableInteger mutableInteger = statusToIndividualsMap.get(individualVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);

	}
}
