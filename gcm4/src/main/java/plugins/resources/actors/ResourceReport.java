package plugins.resources.actors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.ReportContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.events.RegionResourceUpdateEvent;
import plugins.resources.events.ResourceIdAdditionEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.errors.ContractException;

/**
 * A periodic Report that displays the creation, transfer or consumption of
 * resources within a region. Only activities with non-zero action counts are
 * reported.
 *
 *
 * Fields
 *
 * region -- the region identifier
 * 
 * resource -- the resource identifier
 *
 * activity -- the activity that leads to the creation, transfer or consumption
 * of a resource unit(s)
 *
 * actions -- the number of individual actions that were associated with the
 * activity
 *
 * items -- the number of units of the resource that were associated with the
 * activity
 *
 *
 *
 * Activities
 *
 * person_addition -- the addition of a person to the simulation
 *
 * person_departure -- the removal of a person from the simulation
 *
 * person_region_arrival -- the arrival of a person into the region from another
 * region
 *
 * person_region_departure -- the departure of a person from the region to
 * another region
 *
 * region_resource_addition -- the creation of a resource unit(s) on the region
 *
 * person_resource_addition -- the creation of a resource unit(s) on a
 * person(associate with simulation bootstrap)
 *
 * region_resource_removal -- the destruction of a resource unit(s) on the
 * region
 *
 * resource_transfer_into_region -- the transfer of units of resource from
 * another region
 *
 * resource_transfer_out_of_region -- the transfer of units of resource to
 * another region
 *
 * resource_transfer_from_person -- the return of resource units from a person
 * in the region to the region
 *
 * resource_transfer_to_person -- the distribution of resource units to a person
 * in the region from the region
 *
 * resource_removal_from_person -- the destruction of a resource unit(s) on a
 * person
 *
 *
 *
 */
public final class ResourceReport extends PeriodicReport {

	public ResourceReport(ReportLabel reportLabel, ReportPeriod reportPeriod, ResourceId... resourceIds) {
		super(reportLabel, reportPeriod);
		for (ResourceId resourceId : resourceIds) {
			this.resourceIds.add(resourceId);
		}

	}

	private static enum Activity {
		PERSON_ARRIVAL("person_arrival"),
		PERSON_DEPARTURE("person_departure"),
		PERSON_REGION_ARRIVAL("person_region_arrival"),
		PERSON_REGION_DEPARTURE("person_region_departure"),
		REGION_RESOURCE_ADDITION("region_resource_addition"),
		PERSON_RESOURCE_ADDITION("person_resource_addition"),
		REGION_RESOURCE_REMOVAL("region_resource_removal"),
		RESOURCE_TRANSFER_INTO_REGION("resource_transfer_into_region"),
		RESOURCE_TRANSFER_OUT_OF_REGION("resource_transfer_out_of_region"),
		RESOURCE_TRANSFER_FROM_MATERIALS_PRODUCER("resource_transfer_from_materials_producer"),
		TRANSFER_RESOURCE_FROM_PERSON("transfer_resource_from_person"),
		TRANSFER_RESOURCE_TO_PERSON("transfer_resource_to_person"),
		REMOVE_RESOURCE_FROM_PERSON("remove_resource_from_person");

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

	private boolean subscribedToAllResources;

	/*
	 * The mapping of (Region, Resource, Activity) tuples to counters that
	 * record the number of actions and the number of items handled across those
	 * actions.
	 */
	private final Map<RegionId, Map<ResourceId, Map<Activity, Counter>>> regionMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)	.add("region")//
																	.add("resource")//
																	.add("activity")//
																	.add("actions")//
																	.add("items")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (final RegionId regionId : regionMap.keySet()) {

			final Map<ResourceId, Map<Activity, Counter>> resourceMap = regionMap.get(regionId);
			for (final ResourceId resourceId : resourceMap.keySet()) {
				final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
				for (final Activity activity : activityMap.keySet()) {
					final Counter counter = activityMap.get(activity);
					if (counter.actionCount > 0) {
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportLabel(getReportLabel());
						fillTimeFields(reportItemBuilder);

						reportItemBuilder.addValue(regionId.toString());
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

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.personId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(regionId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
			}
		}
	}

	private void handlePersonImminentRemovalEvent(ReportContext reportContext, PersonImminentRemovalEvent personImminentRemovalEvent) {

		PersonId personId = personImminentRemovalEvent.personId();
		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (ResourceId resourceId : resourceIds) {
			final Long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(regionId, resourceId, Activity.PERSON_DEPARTURE, personResourceLevel);
			}
		}
	}

	private void handlePersonResourceUpdateEvent(ReportContext reportContext, PersonResourceUpdateEvent personResourceUpdateEvent) {

		final PersonId personId = personResourceUpdateEvent.personId();
		final ResourceId resourceId = personResourceUpdateEvent.resourceId();
		final long previousLevel = personResourceUpdateEvent.previousResourceLevel();
		final long currentLevel = personResourceUpdateEvent.currentResourceLevel();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		long amount = currentLevel - previousLevel;
		if (amount > 0) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, resourceId, Activity.PERSON_RESOURCE_ADDITION, amount);
		} else {
			amount = -amount;
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, resourceId, Activity.REMOVE_RESOURCE_FROM_PERSON, amount);
		}
	}

	private void handlePersonRegionUpdateEvent(ReportContext reportContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.currentRegionId();

		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				increment(currentRegionId, resourceId, Activity.PERSON_REGION_ARRIVAL, personResourceLevel);
				increment(previousRegionId, resourceId, Activity.PERSON_REGION_DEPARTURE, personResourceLevel);
			}
		}
	}

	private void handleResourceIdAdditionEvent(ReportContext reportContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {

		if (subscribedToAllResources) {
			ResourceId resourceId = resourceIdAdditionEvent.resourceId();
			if (resourceId != null && !resourceIds.contains(resourceId)) {
				resourceIds.add(resourceId);
				for (RegionId regionId : regionMap.keySet()) {
					Map<ResourceId, Map<Activity, Counter>> map = regionMap.get(regionId);
					Map<Activity, Counter> activityMap = new LinkedHashMap<>();
					for (Activity activity : Activity.values()) {
						activityMap.put(activity, new Counter());
					}
					map.put(resourceId, activityMap);
				}
			}
		}
	}

	private void handleRegionResourceUpdateEvent(ReportContext reportContext, RegionResourceUpdateEvent regionResourceUpdateEvent) {

		ResourceId resourceId = regionResourceUpdateEvent.resourceId();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		RegionId regionId = regionResourceUpdateEvent.regionId();
		long previousResourceLevel = regionResourceUpdateEvent.previousResourceLevel();
		long currentResourceLevel = regionResourceUpdateEvent.currentResourceLevel();
		long amount = currentResourceLevel - previousResourceLevel;
		if (amount > 0) {
			increment(regionId, resourceId, Activity.REGION_RESOURCE_ADDITION, amount);
		} else {
			amount = -amount;
			increment(regionId, resourceId, Activity.REGION_RESOURCE_REMOVAL, amount);
		}
	}

	/*
	 * Increments the counter for the given tuple
	 */
	private void increment(final RegionId regionId, final ResourceId resourceId, final Activity activity, final long count) {
		final Map<ResourceId, Map<Activity, Counter>> resourceMap = regionMap.get(regionId);
		final Map<Activity, Counter> activityMap = resourceMap.get(resourceId);
		final Counter counter = activityMap.get(activity);
		counter.actionCount++;
		counter.itemCount += count;
	}

	private RegionsDataManager regionsDataManager;
	private ResourcesDataManager resourcesDataManager;

	/**
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
	 *             resource id passed to the constructor is unknown
	 *             <li>
	 * 
	 */
	@Override
	public void init(final ReportContext reportContext) {
		super.init(reportContext);
		resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
		subscribe(RegionResourceUpdateEvent.class, this::handleRegionResourceUpdateEvent);
		subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);

		if (resourceIds.size() == 0) {
			resourceIds.addAll(resourcesDataManager.getResourceIds());
		}
		/*
		 * Ensure that every client supplied resource identifier is valid
		 */
		final Set<ResourceId> validResourceIds = resourcesDataManager.getResourceIds();
		for (final ResourceId resourceId : resourceIds) {
			if (!validResourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
			}
		}

		subscribe(PersonResourceUpdateEvent.class, this::handlePersonResourceUpdateEvent);
		subscribedToAllResources = true;

		subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);

		/*
		 * Filling the region map with empty counters
		 */
		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			final Map<ResourceId, Map<Activity, Counter>> resourceMap = new LinkedHashMap<>();
			regionMap.put(regionId, resourceMap);
			for (final ResourceId resourceId : resourceIds) {
				final Map<Activity, Counter> activityMap = new LinkedHashMap<>();
				resourceMap.put(resourceId, activityMap);
				for (final Activity activity : Activity.values()) {
					final Counter counter = new Counter();
					activityMap.put(activity, counter);
				}
			}
		}

		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);

			for (final ResourceId resourceId : resourceIds) {
				final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				if (personResourceLevel > 0) {
					increment(regionId, resourceId, Activity.PERSON_ARRIVAL, personResourceLevel);
				}
			}
		}

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
				if (resourceIds.contains(resourceId)) {
					increment(regionId, resourceId, Activity.REGION_RESOURCE_ADDITION, regionResourceLevel);
				}
			}
		}
	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		final Map<ResourceId, Map<Activity, Counter>> resourceMap = new LinkedHashMap<>();
		regionMap.put(regionId, resourceMap);
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