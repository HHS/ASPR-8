package lessons.lesson_12.plugins.vaccine;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lessons.lesson_12.plugins.family.FamilyAdditionEvent;
import lessons.lesson_12.plugins.family.FamilyDataManager;
import lessons.lesson_12.plugins.family.FamilyId;
import lessons.lesson_12.plugins.family.FamilyMemberShipAdditionEvent;
import lessons.lesson_12.plugins.person.PersonAdditionEvent;
import lessons.lesson_12.plugins.person.PersonDataManager;
import lessons.lesson_12.plugins.person.PersonId;
import nucleus.ActorContext;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

public class FamilyVaccineReport {

	private final ReportId reportId;

	private ActorContext actorContext;

	public FamilyVaccineReport(ReportId reportId) {
		this.reportId = reportId;
	}

	private VaccinationDataManager vaccinationDataManager;

	private FamilyDataManager familyDataManager;

	private Map<FamilyVaccineStatus, Set<FamilyId>> statusToFamiliesMap = new LinkedHashMap<>();
	private Map<FamilyId, FamilyVaccineStatus> familyToStatusMap = new LinkedHashMap<>();

	private Map<IndividualVaccineStatus, Set<PersonId>> statusToIndividualsMap = new LinkedHashMap<>();
	private Map<PersonId, IndividualVaccineStatus> individualToStatusMap = new LinkedHashMap<>();

	private enum FamilyVaccineStatus {
		NONE("unvacinated_families"), PARTIAL("partially_vaccinated_families"), FULL("fully_vaccinated_families");

		private final String description;

		private FamilyVaccineStatus(String description) {
			this.description = description;
		}
	}

	private enum IndividualVaccineStatus {
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
			statusToFamiliesMap.get(currentStatus).remove(familyId);
		}
		statusToFamiliesMap.get(newStatus).add(familyId);
		familyToStatusMap.put(familyId, newStatus);
		releaseReportItem();
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
			statusToIndividualsMap.get(currentStatus).remove(personId);
		}
		statusToIndividualsMap.get(newStatus).add(personId);
		individualToStatusMap.put(personId, newStatus);
		releaseReportItem();
	}

	public void init(ActorContext actorContext) {

		this.actorContext = actorContext;
		/*
		 * Subscribe to all the relevant events
		 */
		actorContext.subscribe(VaccinationEvent.class, this::handleVaccinationEvent);
		actorContext.subscribe(FamilyAdditionEvent.class, this::handleFamilyAdditionEvent);
		actorContext.subscribe(FamilyMemberShipAdditionEvent.class, this::handleFamilyMemberShipAdditionEvent);
		actorContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);

		/*
		 * Some of the events may have already occurred before we initialize
		 * this report, so we will need to build up out status maps
		 */

		familyDataManager = actorContext.getDataManager(FamilyDataManager.class);
		vaccinationDataManager = actorContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);

		for (FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
			statusToFamiliesMap.put(familyVaccineStatus, new LinkedHashSet<>());
		}

		for (IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
			statusToIndividualsMap.put(individualVaccineStatus, new LinkedHashSet<>());
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
		refreshFamilyStatus(familyMemberShipAdditionEvent.getFamilyId());
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder builder = ReportHeader.builder();
			builder.add("time");
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

	private void releaseReportItem() {
		ReportItem.Builder builder = ReportItem.builder().setReportId(reportId).setReportHeader(getReportHeader());
		builder.addValue(actorContext.getTime());
		for (FamilyVaccineStatus familyVaccineStatus : statusToFamiliesMap.keySet()) {
			Set<FamilyId> families = statusToFamiliesMap.get(familyVaccineStatus);
			builder.addValue(families.size());
		}
		for (IndividualVaccineStatus individualVaccineStatus : statusToIndividualsMap.keySet()) {
			Set<PersonId> individuals = statusToIndividualsMap.get(individualVaccineStatus);
			builder.addValue(individuals.size());
		}
		ReportItem reportItem = builder.build();
		actorContext.releaseOutput(reportItem);
	}
}
