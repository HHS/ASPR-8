package manual.demo.report;

import nucleus.ReportContext;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.support.GroupId;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonId;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.support.ResourceId;

public class PersonLifeReport {
	private ReportHeader reportHeader;

	private PersonId personId;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			/*
			 * Only the columns that this report is adding are put into the
			 * header. The header portions for the scenario, the replication and
			 * the experiment columns are automatically generated.
			 */
			reportHeader = ReportHeader	.builder()//
										.add("Person")//
										.add("Time")//
										.add("Activity")//
										.add("Details")//
										.build();//
		}
		return reportHeader;
	}

	private CompartmentLocationDataView compartmentLocationDataView;
	private RegionLocationDataView regionLocationDataView;

	public PersonLifeReport(PersonId personId) {
		this.personId = personId;
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(PersonPropertyChangeObservationEvent.class, this::handlePersonPropertyChangeObservationEvent);
		reportContext.subscribe(PersonResourceChangeObservationEvent.class, this::handlePersonResourceChangeObservationEvent);
		reportContext.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		reportContext.subscribe(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);
		reportContext.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);
		reportContext.subscribe(PersonRegionChangeObservationEvent.class, this::handlePersonRegionChangeObservationEvent);
		reportContext.subscribe(GroupMembershipAdditionObservationEvent.class, this::handleGroupMembershipAdditionObservationEvent);
		reportContext.subscribe(GroupMembershipRemovalObservationEvent.class, this::handleGroupMembershipRemovalObservationEvent);

		compartmentLocationDataView = reportContext.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = reportContext.getDataView(RegionLocationDataView.class).get();

		/*
		 * If we can't find a personId, assume it is person 0.
		 */
		if (personId == null) {
			personId = new PersonId(0);
		}
	}

	private void writeReportItem(ReportContext reportContext, Object activity, Object details) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		/*
		 * First add the standard parts of every report item: header, the report
		 * class reference, the scenario id and the replication id
		 */
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());

		/*
		 * Now write the data that matches the header
		 */
		reportItemBuilder.addValue(personId);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(activity);
		reportItemBuilder.addValue(details);

		/*
		 * Finally, release the report item to the environment. The report item
		 * will be sent to NIOReportItemHandler and written to the appropriate
		 * file.
		 */
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	private boolean personIsEligible(PersonId personId) {
		return this.personId.equals(personId);
	}

	private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId sourceCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		if (personIsEligible(personId)) {
			CompartmentId personCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			writeReportItem(reportContext, "Compart Assignment", sourceCompartmentId + " --> " + personCompartmentId);
		}
	}

	private void handlePersonRegionChangeObservationEvent(ReportContext reportContext, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId sourceRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		if (personIsEligible(personId)) {
			RegionId personRegionId = regionLocationDataView.getPersonRegion(personId);
			writeReportItem(reportContext, "Region Assignment", sourceRegionId + " --> " + personRegionId);
		}
	}

	private void handleGroupMembershipAdditionObservationEvent(ReportContext reportContext, GroupMembershipAdditionObservationEvent groupMembershipAdditionObservationEvent) {
		PersonId personId = groupMembershipAdditionObservationEvent.getPersonId();
		if (personIsEligible(personId)) {
			GroupId groupId = groupMembershipAdditionObservationEvent.getGroupId();
			writeReportItem(reportContext, "Joins Group", groupId);
		}
	}

	private void handleGroupMembershipRemovalObservationEvent(ReportContext reportContext, GroupMembershipRemovalObservationEvent groupMembershipRemovalObservationEvent) {
		PersonId personId = groupMembershipRemovalObservationEvent.getPersonId();
		GroupId groupId = groupMembershipRemovalObservationEvent.getGroupId();
		if (personIsEligible(personId)) {
			writeReportItem(reportContext, "Leaves Group", groupId);
		}
	}

	private void handlePersonCreationObservationEvent(ReportContext reportContext, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		if (personIsEligible(personId)) {
			writeReportItem(reportContext, "Added to Simulation", personId);
		}
	}

	private void handlePersonImminentRemovalObservationEvent(ReportContext reportContext, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		if (personIsEligible(personImminentRemovalObservationEvent.getPersonId())) {
			writeReportItem(reportContext, "Removed from Simulation", personImminentRemovalObservationEvent.getPersonId());
		}
	}

	private void handlePersonPropertyChangeObservationEvent(ReportContext reportContext, PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonId personId = personPropertyChangeObservationEvent.getPersonId();
		if (personIsEligible(personId)) {
			PersonPropertyId personPropertyId = personPropertyChangeObservationEvent.getPersonPropertyId();
			Object previousPropertyValue = personPropertyChangeObservationEvent.getPreviousPropertyValue();
			Object personPropertyValue = personPropertyChangeObservationEvent.getCurrentPropertyValue();
			writeReportItem(reportContext, "Property Value Update", personPropertyId + ": " + previousPropertyValue + "-->" + personPropertyValue);
		}
	}

	private void handlePersonResourceChangeObservationEvent(ReportContext reportContext, PersonResourceChangeObservationEvent personResourceChangeObservationEvent) {
		PersonId personId = personResourceChangeObservationEvent.getPersonId();
		if (personIsEligible(personId)) {
			long currentLevel = personResourceChangeObservationEvent.getCurrentResourceLevel();
			long previousLevel = personResourceChangeObservationEvent.getPreviousResourceLevel();
			ResourceId resourceId = personResourceChangeObservationEvent.getResourceId();
			long amount = currentLevel - previousLevel;
			if (amount > 0) {
				writeReportItem(reportContext, "Resource addition", resourceId + ": " + amount);
			} else {
				amount *= -1;
				writeReportItem(reportContext, "Resource removal", resourceId + ": " + amount);
			}
		}

	}

}
