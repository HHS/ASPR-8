package plugins.resources.actors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventFilter;
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
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;
import plugins.resources.events.ResourceIdAdditionEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.errors.ContractException;

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
 * people_with_resource -- the number of people in the region who have at least
 * one unit of the given resource
 *
 * people_without_resource -- the number of people in the region pair who do not
 * have any units of the given resource
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
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
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

		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (ResourceId resourceId : resourceIds) {
			Long amount = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
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
		ResourceId resourceId = personResourceUpdateEvent.resourceId();
		if (!resourceIds.contains(resourceId)) {
			return;
		}
		PersonId personId = personResourceUpdateEvent.personId();
		long currentLevel = personResourceUpdateEvent.currentResourceLevel();
		long previousLevel = personResourceUpdateEvent.previousResourceLevel();
		long amount = currentLevel - previousLevel;

		if (amount == 0) {
			return;
		}
		if (amount > 0) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel == amount) {
				final RegionId regionId = regionsDataManager.getPersonRegion(personId);

				if (reportPeopleWithoutResources) {
					remove(regionId, resourceId, InventoryType.ZERO, personId);
				}
				add(regionId, resourceId, InventoryType.POSITIVE, personId);
			}
		} else {
			amount = -amount;
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel == 0) {
				final RegionId regionId = regionsDataManager.getPersonRegion(personId);
				remove(regionId, resourceId, InventoryType.POSITIVE, personId);
				if (reportPeopleWithoutResources) {
					add(regionId, resourceId, InventoryType.ZERO, personId);
				}
			}
		}

	}

	private void handlePersonRegionUpdateEvent(ActorContext actorContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.currentRegionId();

		for (final ResourceId resourceId : resourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
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

	private RegionsDataManager regionsDataManager;
	private ResourcesDataManager resourcesDataManager;

	/**
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
	 *             resource id passed to the constructor is unknown
	 *             <li>
	 * 
	 */
	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);
		resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		
		subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), this::handlePersonAdditionEvent);
		subscribe(peopleDataManager.getEventFilterForPersonImminentRemovalEvent(), this::handlePersonImminentRemovalEvent);
		subscribe(regionsDataManager.getEventFilterForPersonRegionUpdateEvent(), this::handlePersonRegionUpdateEvent);
		subscribe(regionsDataManager.getEventFilterForRegionAdditionEvent(), this::handleRegionAdditionEvent);


		/*
		 * If no resources were selected, then assume that all are desired.
		 */
		if (resourceIds.size() == 0) {
			resourceIds.addAll(resourcesDataManager.getResourceIds());
		}

		/*
		 * Ensure that the resources are valid
		 */
		final Set<ResourceId> validResourceIds = resourcesDataManager.getResourceIds();
		for (final ResourceId resourceId : resourceIds) {
			if (!validResourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
			}
		}

		// If all resources are covered by this report, then subscribe to the
		// event, otherwise subscribe to each resource id
		if (resourceIds.equals(resourcesDataManager.getResourceIds())) {
			EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent();
			subscribe(eventFilter, this::handlePersonResourceUpdateEvent);			
			subscribe(resourcesDataManager.getEventFilterForResourceIdAdditionEvent(), this::handleResourceIdAdditionEvent);
		} else {
			for (ResourceId resourceId : resourceIds) {
				EventFilter<PersonResourceUpdateEvent> eventFilter = resourcesDataManager.getEventFilterForPersonResourceUpdateEvent(resourceId);
				subscribe(eventFilter, this::handlePersonResourceUpdateEvent);
			}
		}

		/*
		 * Build the tuple map to empty sets of people in preparation for people
		 * being added to the simulation
		 */

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {

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
		for (final PersonId personId : peopleDataManager.getPeople()) {
			for (final ResourceId resourceId : resourceIds) {
				final RegionId regionId = regionsDataManager.getPersonRegion(personId);

				final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
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

	private void handleRegionAdditionEvent(ActorContext actorContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		if (!regionMap.containsKey(regionId)) {

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

	private void handleResourceIdAdditionEvent(ActorContext actorContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		if (!resourceIds.contains(resourceId)) {
			resourceIds.add(resourceId);
			for (RegionId regionID : regionMap.keySet()) {
				Map<ResourceId, Map<InventoryType, Set<PersonId>>> map = regionMap.get(regionID);
				Map<InventoryType, Set<PersonId>> invMap = new LinkedHashMap<>();
				for (InventoryType inventoryType : InventoryType.values()) {
					invMap.put(inventoryType, new LinkedHashSet<>());
				}
				map.put(resourceId, invMap);
			}
		}
	}

}