package lesson.plugins.vaccine.reports;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lesson.plugins.family.datamanagers.FamilyDataManager;
import lesson.plugins.family.events.FamilyAdditionEvent;
import lesson.plugins.family.events.FamilyMemberShipAdditionEvent;
import lesson.plugins.family.support.FamilyId;
import lesson.plugins.person.datamanagers.PersonDataManager;
import lesson.plugins.person.events.PersonAdditionEvent;
import lesson.plugins.person.support.PersonId;
import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;
import lesson.plugins.vaccine.events.VaccinationEvent;
import nucleus.ReportContext;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.wrappers.MutableInteger;

public class HourlyVaccineReport extends PeriodicReport {
	/* start code_ref=reports_plugin_hourly_vaccine_constructor */
	public HourlyVaccineReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);

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
	/* end */

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

	/* start code_ref=reports_plugin_hourly_vaccine_initialization */
	protected void prepare(ReportContext reportContext) {

		/*
		 * Subscribe to all the relevant events
		 */

		reportContext.subscribe(VaccinationEvent.class, this::handleVaccinationEvent);
		reportContext.subscribe(FamilyAdditionEvent.class, this::handleFamilyAdditionEvent);
		reportContext.subscribe(FamilyMemberShipAdditionEvent.class, this::handleFamilyMemberShipAdditionEvent);
		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);

		/* end */

		/*
		 * Some of the events may have already occurred before we initialize this
		 * report, so we will need to build up out status maps
		 */

		familyDataManager = reportContext.getDataManager(FamilyDataManager.class);
		vaccinationDataManager = reportContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = reportContext.getDataManager(PersonDataManager.class);

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

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}

	}

	private void handleVaccinationEvent(ReportContext reportContext, VaccinationEvent vaccinationEvent) {
		PersonId personId = vaccinationEvent.getPersonId();

		Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);

		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}

	private void handleFamilyAdditionEvent(ReportContext reportContext, FamilyAdditionEvent familyAdditionEvent) {
		refreshFamilyStatus(familyAdditionEvent.getFamilyId());
	}

	private void handleFamilyMemberShipAdditionEvent(ReportContext reportContext,
			FamilyMemberShipAdditionEvent familyMemberShipAdditionEvent) {
		individualToStatusMap.remove(familyMemberShipAdditionEvent.getPersonId());
		refreshFamilyStatus(familyMemberShipAdditionEvent.getFamilyId());
	}

	private ReportHeader reportHeader;

	@Override
	/* start code_ref=reports_plugin_hourly_vaccine_flush */
	protected void flush(ReportContext reportContext) {
		ReportItem.Builder builder = ReportItem.builder()//
				.setReportLabel(getReportLabel())//
				.setReportHeader(reportHeader);
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
		reportContext.releaseOutput(reportItem);
	}
	/* end */
}
