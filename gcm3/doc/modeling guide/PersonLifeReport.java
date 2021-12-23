package gcm.test.manual.demo.report;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.PersonInfo;
import gcm.output.reports.ReportHeader;
import gcm.output.reports.ReportHeader.ReportHeaderBuilder;
import gcm.output.reports.ReportItem.ReportItemBuilder;
import gcm.output.reports.StateChange;
import gcm.scenario.CompartmentId;
import gcm.scenario.GroupId;
import gcm.scenario.PersonId;
import gcm.scenario.PersonPropertyId;
import gcm.scenario.RegionId;
import gcm.scenario.ResourceId;
import gcm.simulation.ObservableEnvironment;

public class PersonLifeReport extends AbstractReport {
	private ReportHeader reportHeader;

	private PersonId personId;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			/*
			 * Only the columns that this report is adding are put into the
			 * header. The header portions for the scenario, the replication and
			 * the experiment columns are automatically generated.
			 */
			ReportHeaderBuilder reportHeaderBuilder = new ReportHeaderBuilder();
			reportHeaderBuilder.add("Person");
			reportHeaderBuilder.add("Time");
			reportHeaderBuilder.add("Activity");
			reportHeaderBuilder.add("Details");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	@Override
	public Set<StateChange> getListenedStateChanges() {
		final Set<StateChange> result = new LinkedHashSet<>();
		/*
		 * Add only the state changes that are needed for this report. For each
		 * such state change there will need to be one corresponding handle()
		 * method added to this class. Both the StateChange and Report classes
		 * contain documentation on this relationship.
		 */
		result.add(StateChange.COMPARTMENT_ASSIGNMENT);
		result.add(StateChange.GROUP_MEMBERSHIP_ADDITION);
		result.add(StateChange.GROUP_MEMBERSHIP_REMOVAL);
		result.add(StateChange.PERSON_ADDITION);
		result.add(StateChange.PERSON_REMOVAL);
		result.add(StateChange.PERSON_PROPERTY_VALUE_ASSIGNMENT);
		result.add(StateChange.PERSON_RESOURCE_ADDITION);
		result.add(StateChange.PERSON_RESOURCE_REMOVAL);
		result.add(StateChange.PERSON_RESOURCE_TRANSFER_TO_REGION);
		result.add(StateChange.REGION_RESOURCE_TRANSFER_TO_PERSON);
		result.add(StateChange.REGION_ASSIGNMENT);

		return result;
	}

	@Override
	public void init(final ObservableEnvironment observableEnvironment, Set<Object> initialData) {
		super.init(observableEnvironment, initialData);
		/*
		 * This report is focused on the activities of a single person. The
		 * person id should be contained in the initial data.
		 */
		for (Object initialDatum : initialData) {
			if (initialDatum instanceof PersonId) {
				personId = (PersonId) initialDatum;
			}
		}
		/*
		 * If we can't find a personId, assume it is person 0.
		 */
		if (personId == null) {
			personId = new PersonId(0);
		}
	}

	private void writeReportItem(ObservableEnvironment observableEnvironment, Object activity, Object details) {
		final ReportItemBuilder reportItemBuilder = new ReportItemBuilder();
		/*
		 * First add the standard parts of every report item: header, the report
		 * class reference, the scenario id and the replication id
		 */
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportType(getClass());
		reportItemBuilder.setScenarioId(observableEnvironment.getScenarioId());
		reportItemBuilder.setReplicationId(observableEnvironment.getReplicationId());

		/*
		 * Now write the data that matches the header
		 */
		reportItemBuilder.addValue(personId);
		reportItemBuilder.addValue(observableEnvironment.getTime());
		reportItemBuilder.addValue(activity);
		reportItemBuilder.addValue(details);

		/*
		 * Finally, release the report item to the environment. The report item
		 * will be sent to NIOReportItemHandler and written to the appropriate
		 * file.
		 */
		observableEnvironment.releaseOutputItem(reportItemBuilder.build());
	}

	private boolean personIsEligible(PersonId personId) {
		return this.personId.equals(personId);
	}

	@Override
	public void handleCompartmentAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final CompartmentId sourceCompartmentId) {
		if (personIsEligible(personId)) {
			CompartmentId personCompartmentId = observableEnvironment.getPersonCompartment(personId);
			writeReportItem(observableEnvironment, "Compart Assignment", sourceCompartmentId + " --> " + personCompartmentId);
		}
	}

	@Override
	public void handleRegionAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final RegionId sourceRegionId) {
		if (personIsEligible(personId)) {
			RegionId personRegionId = observableEnvironment.getPersonRegion(personId);
			writeReportItem(observableEnvironment, "Region Assignment", sourceRegionId + " --> " + personRegionId);
		}
	}

	@Override
	public void handleGroupMembershipAddition(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Joins Group", groupId);
		}
	}

	@Override
	public void handleGroupMembershipRemoval(ObservableEnvironment observableEnvironment, GroupId groupId, PersonId personId) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Leaves Group", groupId);
		}
	}

	@Override
	public void handlePersonAddition(ObservableEnvironment observableEnvironment, final PersonId personId) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Added to Simulation", personId);
		}
	}

	@Override
	public void handlePersonRemoval(ObservableEnvironment observableEnvironment, PersonInfo personInfo) {
		if (personIsEligible(personInfo.getPersonId())) {
			writeReportItem(observableEnvironment, "Removed from Simulation", personInfo.getPersonId());
		}
	}

	@Override
	public void handlePersonPropertyValueAssignment(ObservableEnvironment observableEnvironment, final PersonId personId, final PersonPropertyId personPropertyId,
			final Object oldPersonPropertyValue) {
		if (personIsEligible(personId)) {
			Object personPropertyValue = observableEnvironment.getPersonPropertyValue(personId, personPropertyId);
			writeReportItem(observableEnvironment, "Property Value Update", personPropertyId + ": " + oldPersonPropertyValue + "-->" + personPropertyValue);
		}
	}

	@Override
	public void handlePersonResourceAddition(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Resource addition", resourceId + ": " + amount);
		}
	}

	@Override
	public void handlePersonResourceRemoval(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Resource removal", resourceId + ": " + amount);
		}
	}

	@Override
	public void handleRegionResourceTransferToPerson(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Resource transfer to person", resourceId + ": " + amount);
		}
	}

	@Override
	public void handlePersonResourceTransferToRegion(ObservableEnvironment observableEnvironment, final PersonId personId, final ResourceId resourceId, final long amount) {
		if (personIsEligible(personId)) {
			writeReportItem(observableEnvironment, "Resource transfer from person", resourceId + ": " + amount);
		}
	}

}
