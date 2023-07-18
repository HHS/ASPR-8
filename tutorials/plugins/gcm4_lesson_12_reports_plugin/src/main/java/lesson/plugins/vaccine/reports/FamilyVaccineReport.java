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
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import util.wrappers.MutableInteger;

public class FamilyVaccineReport {

	/* start code_ref=reports_plugin_family_vaccine_report_enums */
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
	/* end */

	/* start code_ref=reports_plugin_family_vaccine_report_fields */
	private final ReportLabel reportLabel;

	private ReportHeader reportHeader;

	private ReportContext reportContext;

	private VaccinationDataManager vaccinationDataManager;

	private FamilyDataManager familyDataManager;

	private final Map<FamilyVaccineStatus, MutableInteger> statusToFamiliesMap = new LinkedHashMap<>();

	private final Map<FamilyId, FamilyVaccineStatus> familyToStatusMap = new LinkedHashMap<>();

	private final Map<IndividualVaccineStatus, MutableInteger> statusToIndividualsMap = new LinkedHashMap<>();

	private final Map<PersonId, IndividualVaccineStatus> individualToStatusMap = new LinkedHashMap<>();
	/* end */

	/* start code_ref=reports_plugin_family_vaccine_report_constructor */
	public FamilyVaccineReport(final ReportLabel reportLabel) {
		this.reportLabel = reportLabel;

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
	/* end */

	/* start code_ref=reports_plugin_family_vaccine_report_handling_events */
	private void handleFamilyAdditionEvent(final ReportContext reportContext,
			final FamilyAdditionEvent familyAdditionEvent) {
		refreshFamilyStatus(familyAdditionEvent.getFamilyId());
	}

	private void handleFamilyMemberShipAdditionEvent(final ReportContext reportContext,
			final FamilyMemberShipAdditionEvent familyMemberShipAdditionEvent) {
		individualToStatusMap.remove(familyMemberShipAdditionEvent.getPersonId());
		refreshFamilyStatus(familyMemberShipAdditionEvent.getFamilyId());
	}

	private void handlePersonAdditionEvent(final ReportContext reportContext,
			final PersonAdditionEvent personAdditionEvent) {
		final PersonId personId = personAdditionEvent.getPersonId();
		final Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);
		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			final FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}

	private void handleVaccinationEvent(final ReportContext reportContext, final VaccinationEvent vaccinationEvent) {
		final PersonId personId = vaccinationEvent.getPersonId();

		final Optional<FamilyId> optional = familyDataManager.getFamilyId(personId);

		if (optional.isEmpty()) {
			refreshIndividualStatus(personId);
		} else {
			final FamilyId familyId = optional.get();
			refreshFamilyStatus(familyId);
		}
	}
	/* end */

	/* start code_ref=reports_plugin_family_vaccine_report_init_subscriptions */
	public void init(final ReportContext reportContext) {
		this.reportContext = reportContext;
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

		/* start code_ref=reports_plugin_family_vaccine_report_init_setup */
		familyDataManager = reportContext.getDataManager(FamilyDataManager.class);
		vaccinationDataManager = reportContext.getDataManager(VaccinationDataManager.class);
		PersonDataManager personDataManager = reportContext.getDataManager(PersonDataManager.class);

		for (final FamilyVaccineStatus familyVaccineStatus : FamilyVaccineStatus.values()) {
			statusToFamiliesMap.put(familyVaccineStatus, new MutableInteger());
		}

		for (final IndividualVaccineStatus individualVaccineStatus : IndividualVaccineStatus.values()) {
			statusToIndividualsMap.put(individualVaccineStatus, new MutableInteger());
		}
		/* end */

		// determine the family vaccine status for every family
		/* start code_ref=reports_plugin_family_vaccine_report_init_family_status */
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
		/* end */

		// ensure that any person not assigned to a family is still counted

		/* start code_ref=reports_plugin_family_vaccine_report_init_person_status */
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
		/* end */

		/* start code_ref=reports_plugin_family_vaccine_report_init_releasing_output */
		releaseReportItem();
		/* end */
	}

	/*
	 * start code_ref=reports_plugin_family_vaccine_report_refeshing_family_status
	 */
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
	/* end */

	/*
	 * start
	 * code_ref=reports_plugin_family_vaccine_report_refeshing_individual_status
	 */
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
	/* end */

	/*
	 * start
	 * code_ref=reports_plugin_family_vaccine_report_init_releasing_report_item
	 */
	private void releaseReportItem() {
		final ReportItem.Builder builder = ReportItem.builder().setReportLabel(reportLabel)
				.setReportHeader(reportHeader);
		builder.addValue(reportContext.getTime());
		for (final FamilyVaccineStatus familyVaccineStatus : statusToFamiliesMap.keySet()) {
			MutableInteger mutableInteger = statusToFamiliesMap.get(familyVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		for (final IndividualVaccineStatus individualVaccineStatus : statusToIndividualsMap.keySet()) {
			MutableInteger mutableInteger = statusToIndividualsMap.get(individualVaccineStatus);
			builder.addValue(mutableInteger.getValue());
		}
		final ReportItem reportItem = builder.build();
		reportContext.releaseOutput(reportItem);
	}
	/* end */
}
