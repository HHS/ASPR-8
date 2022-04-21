package plugins.resources.actors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventLabel;
import plugins.people.PersonDataManager;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.support.ResourceId;

/**
 * A periodic Report that displays number of people who have/do not have any
 * units of a particular resource with a region.
 *
 *
 * Fields
 *
 * region -- the region identifier
 *
 * Resource -- the resource identifier
 *
 * people_with_resource -- the number of people in the region who
 * have at least one unit of the given resource
 *
 * people_without_resource -- the number of people in the region pair
 * who do not have any units of the given resource
 *
 * @author Shawn Hatch
 *
 */
public final class PersonResourceReport extends PeriodicReport {
	public PersonResourceReport(ReportId reportId, ReportPeriod reportPeriod, boolean reportPeopleWithoutResources, boolean reportZeroPopulations, ResourceId... resourceIds) {
		super(reportId, reportPeriod);
		this.reportPeopleWithoutResources = reportPeopleWithoutResources;
		this.reportZeroPopulations = reportZeroPopulations;
		for (ResourceId resourceId : resourceIds) {
			this.resourceIds.add(resourceId);
		}
	}

	/**
	 * An enmeration mirroring the differentiation in the report for populations
	 * of people with and without a resource.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	private static enum InventoryType {
		ZERO, POSITIVE
	}

	/*
	 * The resources that will be used in this report. They are derived from the
	 * values passed in the init() method.
	 */
	private Set<ResourceId> resourceIds = new LinkedHashSet<>();

	/*
	 * Boolean for controlling the reporting of people with out resources. Set
	 * in the init() method.
	 */
	private boolean reportPeopleWithoutResources;

	/*
	 * Boolean for controlling the reporting of people with out resources. Set
	 * in the init() method.
	 */
	private boolean reportZeroPopulations;

	// Mapping of the (regionId, resource Id, InventoryType) to
	// sets of person id. Maintained via the processing of events.
	private final Map<RegionId, Map<ResourceId, Map<InventoryType, Set<PersonId>>>> regionMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder)//
			.add("region")//			
			.add("resource")//
			.add("people_with_resource");
			if (reportPeopleWithoutResources) {
				reportHeaderBuilder.add("people_without_resource");
			}
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	/*
	 * Adds a person to the set of people associated with the given tuple
	 */
	private void add(final RegionId regionId, final ResourceId resourceId, final InventoryType inventoryType, final PersonId personId) {
		final Set<PersonId> people = regionMap.get(regionId).get(resourceId).get(inventoryType);
		people.add(personId);
	}

	@Override
	protected void flush(ActorContext actorContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (final RegionId regionId : regionMap.keySet()) {
			final Map<ResourceId, Map<InventoryType, Set<PersonId>>> resourceMap = regionMap.get(regionId);
			for (final ResourceId resourceId : resourceIds) {
				final Map<InventoryType, Set<PersonId>> inventoryMap = resourceMap.get(resourceId);

				final int positiveCount = inventoryMap.get(InventoryType.POSITIVE).size();
				int count = positiveCount;
				final int zeroCount = inventoryMap.get(InventoryType.ZERO).size();
				if (reportPeopleWithoutResources) {
					count += zeroCount;
				}
				final boolean shouldReport = reportZeroPopulations || (count > 0);

				if (shouldReport) {
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportId(getReportId());

					fillTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());
					reportItemBuilder.addValue(resourceId.toString());
					reportItemBuilder.addValue(positiveCount);
					if (reportPeopleWithoutResources) {
						reportItemBuilder.addValue(zeroCount);
					}
					actorContext.releaseOutput(reportItemBuilder.build());
				}
			}

		}
	}

	private void handlePersonAdditionEvent(ActorContext actorContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		final RegionId regionId = regionDataManager.getPersonRegion(personId);

		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				add(regionId, resourceId, InventoryType.POSITIVE, personId);
			} else {
				if (reportPeopleWithoutResources) {
					add(regionId, resourceId, InventoryType.ZERO, personId);
				}
			}
		}
	}

	private void handlePersonImminentRemovalEvent(ActorContext actorContext, PersonImminentRemovalEvent personImminentRemovalEvent) {

		PersonId personId = personImminentRemovalEvent.getPersonId();

		RegionId regionId = regionDataManager.getPersonRegion(personId);

		for (ResourceId resourceId : resourceIds) {
			Long amount = resourceDataManager.getPersonResourceLevel(resourceId, personId);
			if (amount > 0) {
				remove(regionId, resourceId, InventoryType.POSITIVE, personId);
			} else {
				if (reportPeopleWithoutResources) {
					remove(regionId, resourceId, InventoryType.ZERO, personId);
				}
			}
		}
	}

	private void handlePersonResourceUpdateEvent(ActorContext actorContext, PersonResourceUpdateEvent personResourceUpdateEvent) {
		ResourceId resourceId = personResourceUpdateEvent.getResourceId();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		PersonId personId = personResourceUpdateEvent.getPersonId();
		long currentLevel = personResourceUpdateEvent.getCurrentResourceLevel();
		long previousLevel = personResourceUpdateEvent.getPreviousResourceLevel();
		long amount = currentLevel - previousLevel;

		if (amount == 0) {
			return;
		}
		if (amount > 0) {
			final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel == amount) {
				final RegionId regionId = regionDataManager.getPersonRegion(personId);

				if (reportPeopleWithoutResources) {
					remove(regionId, resourceId, InventoryType.ZERO, personId);
				}
				add(regionId, resourceId, InventoryType.POSITIVE, personId);
			}
		} else {
			amount = -amount;
			final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel == 0) {
				final RegionId regionId = regionDataManager.getPersonRegion(personId);
				remove(regionId, resourceId, InventoryType.POSITIVE, personId);
				if (reportPeopleWithoutResources) {
					add(regionId, resourceId, InventoryType.ZERO, personId);
				}
			}
		}

	}

	private void handlePersonRegionUpdateEvent(ActorContext actorContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.getPersonId();
		RegionId previousRegionId = personRegionUpdateEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.getCurrentRegionId();

		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				remove(previousRegionId, resourceId, InventoryType.POSITIVE, personId);
				add(currentRegionId, resourceId, InventoryType.POSITIVE, personId);
			} else {
				if (reportPeopleWithoutResources) {
					remove(previousRegionId, resourceId, InventoryType.ZERO, personId);
					add(currentRegionId, resourceId, InventoryType.ZERO, personId);
				}
			}
		}
	}

	private RegionDataManager regionDataManager;
	private ResourceDataManager resourceDataManager;

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		actorContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		actorContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		actorContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);

		resourceDataManager = actorContext.getDataManager(ResourceDataManager.class);
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class);
		regionDataManager = actorContext.getDataManager(RegionDataManager.class);
		RegionDataManager regionDataManager = actorContext.getDataManager(RegionDataManager.class);

		/*
		 * If no resources were selected, then assume that all are desired.
		 */
		if (resourceIds.size() == 0) {
			resourceIds.addAll(resourceDataManager.getResourceIds());
		}

		/*
		 * Ensure that the resources are valid
		 */
		final Set<ResourceId> validResourceIds = resourceDataManager.getResourceIds();
		for (final ResourceId resourceId : resourceIds) {
			if (!validResourceIds.contains(resourceId)) {
				throw new RuntimeException("invalid resource id " + resourceId);
			}
		}

		// If all resources are covered by this report, then subscribe to the
		// event, otherwise subscribe to each resource id
		if (resourceIds.equals(resourceDataManager.getResourceIds())) {
			actorContext.subscribe(PersonResourceUpdateEvent.class, this::handlePersonResourceUpdateEvent);
		} else {
			for (ResourceId resourceId : resourceIds) {
				EventLabel<PersonResourceUpdateEvent> eventLabelByResource = PersonResourceUpdateEvent.getEventLabelByResource(actorContext, resourceId);
				actorContext.subscribe(eventLabelByResource, this::handlePersonResourceUpdateEvent);
			}
		}

		/*
		 * Build the tuple map to empty sets of people in preparation for people
		 * being added to the simulation
		 */

		for (final RegionId regionId : regionDataManager.getRegionIds()) {

			final Map<ResourceId, Map<InventoryType, Set<PersonId>>> resourceMap = new LinkedHashMap<>();
			regionMap.put(regionId, resourceMap);

			for (final ResourceId resourceId : resourceIds) {
				final Map<InventoryType, Set<PersonId>> inventoryMap = new LinkedHashMap<>();
				resourceMap.put(resourceId, inventoryMap);
				for (final InventoryType inventoryType : InventoryType.values()) {
					final Set<PersonId> people = new LinkedHashSet<>();
					inventoryMap.put(inventoryType, people);
				}
			}

		}

		/*
		 * Place the initial population in the mapping
		 */
		for (final PersonId personId : personDataManager.getPeople()) {
			for (final ResourceId resourceId : resourceIds) {
				final RegionId regionId = regionDataManager.getPersonRegion(personId);

				final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
				if (personResourceLevel > 0) {
					add(regionId, resourceId, InventoryType.POSITIVE, personId);
				} else {
					if (reportPeopleWithoutResources) {
						add(regionId, resourceId, InventoryType.ZERO, personId);
					}
				}
			}
		}

	}

	/*
	 * Removes a person to the set of people associated with the given tuple
	 */
	private void remove(final RegionId regionId, final ResourceId resourceId, final InventoryType inventoryType, final PersonId personId) {
		if (resourceIds.contains(resourceId)) {
			final Set<PersonId> people = regionMap.get(regionId).get(resourceId).get(inventoryType);
			people.remove(personId);
		}
	}

}