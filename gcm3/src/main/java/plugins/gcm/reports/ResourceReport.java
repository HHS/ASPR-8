package plugins.gcm.reports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nucleus.EventLabel;
import nucleus.ReportContext;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.support.ResourceId;

/**
 * A periodic Report that displays the creation, transfer or consumption of
 * resources within a region/compartment pair. Some activities have no
 * compartment association and will leave the compartment field blank. Only
 * activities with non-zero action counts are reported.
 *
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * Compartment -- the compartment identifier
 *
 * Resource -- the resource identifier
 *
 * Activity -- the activity that leads to the creation, transfer or consumption
 * of a resource unit(s)
 *
 * Actions -- the number of individual actions that were associated with the
 * activity
 *
 * Items -- the number of units of the resource that were associated with the
 * activity
 *
 *
 *
 * Activities
 *
 * PersonAddition -- the addition of a person to the simulation
 *
 * PersonDeparture -- the removal of a person from the simulation
 *
 * PersonRegionArrival -- the arrival of a person into the region from another
 * region
 *
 * PersonRegionDeparture -- the departure of a person from the region to another
 * region
 *
 * PersonCompartmentArrival -- the arrival of a person into the compartment from
 * another compartment
 *
 * PersonCompartmentDeparture -- the departure of a person from the compartment
 * to another compartment
 *
 * RegionResourceAddition -- the creation of a resource unit(s) on the region
 *
 * PersonResourceAddition -- the creation of a resource unit(s) on a
 * person(associate with simulation bootstrap)
 *
 * RegionResourceRemoval -- the destruction of a resource unit(s) on the region
 *
 * ResourceTransferIntoRegion -- the transfer of units of resource from another
 * region
 *
 * ResourceTransferOutOfRegion -- the transfer of units of resource to another
 * region
 *
 * ResourceTransferFromPerson -- the return of resource units from a person in
 * the region to the region
 *
 * ResourceTransferToPerson -- the distribution of resource units to a person in
 * the region from the region
 *
 * ResourceRemovalFromPerson -- the destruction of a resource unit(s) on a
 * person
 *
 *
 * @author Shawn Hatch
 *
 */
public final class ResourceReport extends PeriodicReport {

	public ResourceReport(ReportPeriod reportPeriod, ResourceId... resourceIds) {
		super(reportPeriod);
		for (ResourceId resourceId : resourceIds) {
			this.resourceIds.add(resourceId);
		}

	}

	private static enum Activity {
		PERSON_ARRIVAL("PersonAddition"),
		PERSON_DEPARTURE("PersonDeparture"),
		PERSON_REGION_ARRIVAL("PersonRegionArrival"),
		PERSON_REGION_DEPARTURE("PersonRegionDeparture"),
		PERSON_COMPARTMENT_ARRIVAL("PersonCompartmentArrival"),
		PERSON_COMPARTMENT_DEPARTURE("PersonCompartmentDeparture"),
		REGION_RESOURCE_ADDITION("RegionResourceAddition"),
		PERSON_RESOURCE_ADDITION("PersonResourceAddition"),
		REGION_RESOURCE_REMOVAL("RegionResourceRemoval"),
		RESOURCE_TRANSFER_INTO_REGION("ResourceTransferIntoRegion"),
		RESOURCE_TRANSFER_OUT_OF_REGION("ResourceTransferOutOfRegion"),
		RESOURCE_TRANSFER_FROM_MATERIALS_PRODUCER("ResourceTransferFromMaterialsProducer"),
		TRANSFER_RESOURCE_FROM_PERSON("ResourceTransferFromPerson"),
		TRANSFER_RESOURCE_TO_PERSON("ResourceTransferToPerson"),
		REMOVE_RESOURCE_FROM_PERSON("ResourceRemovalFromPerson");

		private final String displayName;

		Activity(final String displayName) {
			this.displayName = displayName;
		}
	}

	private static class Counter {
		private int actionCount;
		private long itemCount;

		private void reset() {
			actionCount = 0;
			itemCount = 0;
		}
	}

	/*
	 * The resource covered by this report. Determined in the init() method.
	 */
	private final List<ResourceId> resourceIds = new ArrayList<>();

	/*
	 * The mapping of (Region, Compartment, Resource, Activity) tuples to
	 * counters that record the number of actions and the number of items
	 * handled across those actions.
	 */
	private final Map<RegionId, Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>>> regionMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)	.add("Region")//
																	.add("Compartment")//
																	.add("Resource")//
																	.add("Activity")//
																	.add("Actions")//
																	.add("Items")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (final RegionId regionId : regionMap.keySet()) {
			final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = regionMap.get(regionId);
			for (final CompartmentId compartmentId : compartmentMap.keySet()) {
				final Map<ResourceId, Map<Activity, Counter>> resourceMap = compartmentMap.get(compartmentId);
				for (final ResourceId resourceId : resourceMap.keySet()) {
					final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
					for (final Activity activity : activityMap.keySet()) {
						final Counter counter = activityMap.get(activity);
						if (counter.actionCount > 0) {
							reportItemBuilder.setReportHeader(getReportHeader());
							reportItemBuilder.setReportId(reportContext.getCurrentReportId());
							fillTimeFields(reportItemBuilder);

							reportItemBuilder.addValue(regionId.toString());
							if (compartmentId != null) {
								reportItemBuilder.addValue(compartmentId.toString());
							} else {
								reportItemBuilder.addValue("");
							}
							reportItemBuilder.addValue(resourceId.toString());
							reportItemBuilder.addValue(activity.displayName);
							reportItemBuilder.addValue(counter.actionCount);
							reportItemBuilder.addValue(counter.itemCount);
							reportContext.releaseOutput(reportItemBuilder.build());
							counter.reset();
						}
					}
				}
			}
		}
	}

	private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId sourceCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_COMPARTMENT_ARRIVAL, personResourceLevel);
				increment(regionId, sourceCompartmentId, resourceId, Activity.PERSON_COMPARTMENT_DEPARTURE, personResourceLevel);
			}
		}
	}

	private void handlePersonCreationObservationEvent(ReportContext reportContext, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
			}
		}
	}

	private void handlePersonImminentRemovalObservationEvent(ReportContext reportContext, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {

		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		for (ResourceId resourceId : resourceIds) {
			final Long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(regionId, compartmentId, resourceId, Activity.PERSON_DEPARTURE, personResourceLevel);
			}
		}
	}

	private void handlePersonResourceChangeObservationEvent(ReportContext reportContext, PersonResourceChangeObservationEvent personResourceChangeObservationEvent) {

		final PersonId personId = personResourceChangeObservationEvent.getPersonId();
		final ResourceId resourceId = personResourceChangeObservationEvent.getResourceId();
		final long previousLevel = personResourceChangeObservationEvent.getPreviousResourceLevel();
		final long currentLevel = personResourceChangeObservationEvent.getCurrentResourceLevel();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		long amount = currentLevel - previousLevel;
		if (amount > 0) {

			final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.PERSON_RESOURCE_ADDITION, amount);
		} else {
			amount = -amount;

			final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			increment(regionId, compartmentId, resourceId, Activity.REMOVE_RESOURCE_FROM_PERSON, amount);
		}
	}

	private void handlePersonRegionChangeObservationEvent(ReportContext reportContext, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();

		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(currentRegionId, compartmentId, resourceId, Activity.PERSON_REGION_ARRIVAL, personResourceLevel);
				increment(previousRegionId, compartmentId, resourceId, Activity.PERSON_REGION_DEPARTURE, personResourceLevel);
			}
		}
	}

	private void handleRegionResourceChangeObservationEvent(ReportContext reportContext, RegionResourceChangeObservationEvent regionResourceChangeObservationEvent) {

		ResourceId resourceId = regionResourceChangeObservationEvent.getResourceId();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		RegionId regionId = regionResourceChangeObservationEvent.getRegionId();
		long previousResourceLevel = regionResourceChangeObservationEvent.getPreviousResourceLevel();
		long currentResourceLevel = regionResourceChangeObservationEvent.getCurrentResourceLevel();
		long amount = currentResourceLevel - previousResourceLevel;
		if (amount > 0) {
			increment(regionId, null, resourceId, Activity.REGION_RESOURCE_ADDITION, amount);
		} else {
			amount = -amount;
			increment(regionId, null, resourceId, Activity.REGION_RESOURCE_REMOVAL, amount);
		}
	}

	/*
	 * Increments the counter for the given tuple
	 */
	private void increment(final RegionId regionId, final CompartmentId compartmentId, final ResourceId resourceId, final Activity activity, final long count) {
		final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = regionMap.get(regionId);
		final Map<ResourceId, Map<Activity, Counter>> resourceMap = compartmentMap.get(compartmentId);
		final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
		final Counter counter = activityMap.get(activity);
		counter.actionCount++;
		counter.itemCount += count;
	}

	private RegionLocationDataView regionLocationDataView;
	private CompartmentLocationDataView compartmentLocationDataView;
	private ResourceDataView resourceDataView;

	@Override
	public void init(final ReportContext reportContext) {
		super.init(reportContext);

		reportContext.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		reportContext.subscribe(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);
		reportContext.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);
		reportContext.subscribe(PersonRegionChangeObservationEvent.class, this::handlePersonRegionChangeObservationEvent);
		reportContext.subscribe(RegionResourceChangeObservationEvent.class, this::handleRegionResourceChangeObservationEvent);

		resourceDataView = reportContext.getDataView(ResourceDataView.class).get();
		PersonDataView personDataView = reportContext.getDataView(PersonDataView.class).get();
		compartmentLocationDataView = reportContext.getDataView(CompartmentLocationDataView.class).get();
		RegionDataView regionDataView = reportContext.getDataView(RegionDataView.class).get();
		regionLocationDataView = reportContext.getDataView(RegionLocationDataView.class).get();

		if (resourceIds.size() == 0) {
			resourceIds.addAll(resourceDataView.getResourceIds());
		}
		/*
		 * Ensure that every client supplied resource identifier is valid
		 */
		final Set<ResourceId> validResourceIds = resourceDataView.getResourceIds();
		for (final ResourceId resourceId : resourceIds) {
			if (!validResourceIds.contains(resourceId)) {
				throw new RuntimeException("invalid resource id " + resourceId);
			}
		}

		// If all the resources are included in the report, then subscribe to
		// the event, otherwise subscribe to each resource
		if (resourceIds.stream().collect(Collectors.toSet()).equals(resourceDataView.getResourceIds())) {
			reportContext.subscribe(PersonResourceChangeObservationEvent.class, this::handlePersonResourceChangeObservationEvent);
		} else {
			for (ResourceId resourceId : resourceIds) {
				EventLabel<PersonResourceChangeObservationEvent> eventLabelByResource = PersonResourceChangeObservationEvent.getEventLabelByResource(reportContext, resourceId);
				reportContext.subscribe(eventLabelByResource, this::handlePersonResourceChangeObservationEvent);
			}
		}

		/*
		 * We add the null compartment to the set of compartment ids to provide
		 * a place in the region map to house counters that do not correspond to
		 * any compartment.
		 */

		final Set<CompartmentId> compartmentIds = reportContext.getDataView(CompartmentDataView.class).get().getCompartmentIds();

		compartmentIds.add(null);

		/*
		 * Filling the region map with empty counters
		 */
		for (final RegionId regionId : regionDataView.getRegionIds()) {
			final Map<CompartmentId, Map<ResourceId, Map<Activity, Counter>>> compartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, compartmentMap);
			for (final CompartmentId compartmentId : compartmentIds) {
				final Map<ResourceId, Map<Activity, Counter>> resourceMap = new LinkedHashMap<>();
				compartmentMap.put(compartmentId, resourceMap);
				for (final ResourceId resourceId : resourceIds) {
					final Map<Activity, Counter> activityMap = new LinkedHashMap<>();
					resourceMap.put(resourceId, activityMap);
					for (final Activity activity : Activity.values()) {
						final Counter counter = new Counter();
						activityMap.put(activity, counter);
					}
				}
			}
		}

		for (PersonId personId : personDataView.getPeople()) {
			final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			for (final ResourceId resourceId : resourceIds) {
				final long personResourceLevel = resourceDataView.getPersonResourceLevel(resourceId, personId);
				if (personResourceLevel > 0) {
					increment(regionId, compartmentId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
				}
			}
		}

		for (RegionId regionId : regionDataView.getRegionIds()) {
			for (ResourceId resourceId : resourceDataView.getResourceIds()) {
				long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
				if (resourceIds.contains(resourceId)) {
					increment(regionId, null, resourceId, Activity.REGION_RESOURCE_ADDITION, regionResourceLevel);
				}
			}
		}
	}
}