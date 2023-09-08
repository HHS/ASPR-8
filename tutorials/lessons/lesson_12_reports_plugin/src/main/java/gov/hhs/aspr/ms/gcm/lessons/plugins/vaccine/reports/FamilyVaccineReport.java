package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.datamanagers.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.events.FamilyAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.events.FamilyMemberShipAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.support.FamilyId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.datamanagers.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.datamanagers.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.events.VaccinationEvent;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import util.wrappers.MutableInteger;

public class FamilyVaccineReport {

	/* start code_ref=reports_plugin_family_vaccine_report_enums|code_cap=The family vaccine report defines two enums for the vaccination status of families and individuals. */
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

	/* start code_ref=reports_plugin_family_vaccine_report_fields|code_cap=The family vaccine report collects summary data as events unfold and requires a few private data structures to record these events. */
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

	/* start code_ref=reports_plugin_family_vaccine_report_constructor|code_cap=The report initializes its data structures.*/
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

	/* start code_ref=reports_plugin_family_vaccine_report_handling_events|code_cap=The report will need to have handlers for each of the subscribed events. */
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

	/* start code_ref=reports_plugin_family_vaccine_report_init_subscriptions|code_cap=The report must subscribe to the events that are pertinent to reporting individual and family vaccination status.*/
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

		/* start code_ref=reports_plugin_family_vaccine_report_init_setup|code_cap=The local data structures are initialized from the current vaccine states. */
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
		/* start code_ref=reports_plugin_family_vaccine_report_init_family_status|code_cap=Determining vaccine status for each family.*/
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

		/* start code_ref=reports_plugin_family_vaccine_report_init_person_status|code_cap=Capturing individuals who have no family association. */
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

		/* start code_ref=reports_plugin_family_vaccine_report_init_releasing_output|code_cap=The initial state of the report is released as a single report item.*/
		releaseReportItem();
		/* end */
	}

	/*
	 * start code_ref=reports_plugin_family_vaccine_report_refeshing_family_status|code_cap=Events that effect the status of a family are processed centrally.
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
	 * start code_ref=reports_plugin_family_vaccine_report_refeshing_individual_status|code_cap=Events that effect the status of an individual are processed centrally.
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
	 * start code_ref=reports_plugin_family_vaccine_report_init_releasing_report_item|code_cap=Each time a family or individual have a relevant change a report item is released.
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
