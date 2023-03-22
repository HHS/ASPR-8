package plugins.resources.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ReportContext;
import nucleus.SimulationStateContext;
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
import plugins.reports.support.ReportItem;
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
 * resource -- the resource identifier
 *
 * people_with_resource -- the number of people in the region who have at least
 * one unit of the given resource
 *
 * people_without_resource -- the number of people in the region pair who do not
 * have any units of the given resource
 *
 *
 */
public final class PersonResourceReport extends PeriodicReport {
	public PersonResourceReport(PersonResourceReportPluginData personResourceReportPluginData) {
		super(personResourceReportPluginData.getReportLabel(), personResourceReportPluginData.getReportPeriod());
		this.reportPeopleWithoutResources = personResourceReportPluginData.getReportPeopleWithoutResources();
		this.reportZeroPopulations = personResourceReportPluginData.getReportZeroPopulations();
		this.includedResourceIds.addAll(personResourceReportPluginData.getIncludedResourceIds());
		this.excludedResourceIds.addAll(personResourceReportPluginData.getExcludedResourceIds());
		this.includeNewResourceIds = personResourceReportPluginData.getDefaultInclusionPolicy();
	}

	/**
	 * An enmeration mirroring the differentiation in the report for populations
	 * of people with and without a resource.
	 * 
	 *
	 */
	private static enum InventoryType {
		ZERO, POSITIVE
	}

	/*
	 * The resources that will be used in this report. They are derived from the
	 * values passed in the init() method.
	 */

	private final boolean includeNewResourceIds;

	private final Set<ResourceId> includedResourceIds = new LinkedHashSet<>();
	private final Set<ResourceId> currentResourceIds = new LinkedHashSet<>();
	private final Set<ResourceId> excludedResourceIds = new LinkedHashSet<>();
	/*
	 * Boolean for controlling the reporting of people with out resources. Set
	 * in the init() method.
	 */
	private final boolean reportPeopleWithoutResources;

	/*
	 * Boolean for controlling the reporting of people with out resources. Set
	 * in the init() method.
	 */
	private final boolean reportZeroPopulations;

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
	protected void flush(ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		for (final RegionId regionId : regionMap.keySet()) {
			final Map<ResourceId, Map<InventoryType, Set<PersonId>>> resourceMap = regionMap.get(regionId);
			for (final ResourceId resourceId : currentResourceIds) {
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
					reportItemBuilder.setReportLabel(getReportLabel());

					fillTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());
					reportItemBuilder.addValue(resourceId.toString());
					reportItemBuilder.addValue(positiveCount);
					if (reportPeopleWithoutResources) {
						reportItemBuilder.addValue(zeroCount);
					}
					reportContext.releaseOutput(reportItemBuilder.build());
				}
			}

		}
	}

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.personId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (final ResourceId resourceId : currentResourceIds) {
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

	private void handlePersonImminentRemovalEvent(ReportContext reportContext, PersonImminentRemovalEvent personImminentRemovalEvent) {

		PersonId personId = personImminentRemovalEvent.personId();

		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (ResourceId resourceId : currentResourceIds) {
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

	private void handlePersonResourceUpdateEvent(ReportContext reportContext, PersonResourceUpdateEvent personResourceUpdateEvent) {
		ResourceId resourceId = personResourceUpdateEvent.resourceId();
		if (isCurrentProperty(resourceId)) {
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
	}

	private void handlePersonRegionUpdateEvent(ReportContext reportContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.currentRegionId();

		for (final ResourceId resourceId : currentResourceIds) {
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

	private boolean isCurrentProperty(ResourceId resourceId) {
		return currentResourceIds.contains(resourceId);
	}

	private boolean addToCurrentResourceIds(ResourceId resourceId) {

		// There are eight possibilities:

		/*
		 * P -- the default inclusion policy
		 * 
		 * I -- the property is explicitly included
		 * 
		 * X -- the property is explicitly excluded
		 * 
		 * C -- the property should be on the current properties
		 * 
		 * 
		 * P I X C Table
		 * 
		 * TRUE TRUE FALSE TRUE
		 * 
		 * TRUE FALSE FALSE TRUE
		 * 
		 * FALSE TRUE FALSE TRUE
		 * 
		 * FALSE FALSE FALSE FALSE
		 * 
		 * TRUE TRUE TRUE FALSE -- not possible
		 * 
		 * TRUE FALSE TRUE FALSE
		 * 
		 * FALSE TRUE TRUE FALSE -- not possible
		 * 
		 * FALSE FALSE TRUE FALSE
		 * 
		 * 
		 * Two of the cases above are contradictory since a property cannot be
		 * both explicitly included and explicitly excluded
		 * 
		 */

		// if X is true then we don't add the property
		if (excludedResourceIds.contains(resourceId)) {
			return false;
		}

		// if both P and I are false we don't add the property
		boolean included = includedResourceIds.contains(resourceId);

		if (!included && !includeNewResourceIds) {
			return false;
		}

		// we have failed to reject the property
		currentResourceIds.add(resourceId);

		return true;
	}

	/**
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if a
	 *             resource id passed to the constructor is unknown
	 *             <li>
	 * 
	 */
	@Override
	protected void prepare(final ReportContext reportContext) {
		resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		reportContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		reportContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
		reportContext.subscribe(PersonResourceUpdateEvent.class, this::handlePersonResourceUpdateEvent);
		reportContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		reportContext.subscribeToSimulationState(this::recordSimulationState);

		for (final ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			addToCurrentResourceIds(resourceId);
		}

		/*
		 * Build the tuple map to empty sets of people in preparation for people
		 * being added to the simulation
		 */

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {

			final Map<ResourceId, Map<InventoryType, Set<PersonId>>> resourceMap = new LinkedHashMap<>();
			regionMap.put(regionId, resourceMap);

			for (final ResourceId resourceId : currentResourceIds) {
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
			for (final ResourceId resourceId : currentResourceIds) {
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

	private void recordSimulationState(ReportContext reportContext, SimulationStateContext simulationStateContext) {
		PersonResourceReportPluginData.Builder builder = simulationStateContext.get(PersonResourceReportPluginData.Builder.class);
		for (ResourceId resourceId : includedResourceIds) {
			builder.includeResourceId(resourceId);
		}
		for (ResourceId resourceId : excludedResourceIds) {
			builder.excludeResourceId(resourceId);
		}
		builder.setDefaultInclusion(includeNewResourceIds);
		builder.setReportLabel(getReportLabel());
		builder.setReportPeriod(getReportPeriod());
		builder.setReportPeopleWithoutResources(reportPeopleWithoutResources);
		builder.setReportZeroPopulations(reportZeroPopulations);

	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		if (!regionMap.containsKey(regionId)) {

			final Map<ResourceId, Map<InventoryType, Set<PersonId>>> resourceMap = new LinkedHashMap<>();
			regionMap.put(regionId, resourceMap);

			for (final ResourceId resourceId : currentResourceIds) {
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
		if (currentResourceIds.contains(resourceId)) {
			final Set<PersonId> people = regionMap.get(regionId).get(resourceId).get(inventoryType);
			people.remove(personId);
		}
	}

	private void handleResourceIdAdditionEvent(ReportContext reportContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		if (addToCurrentResourceIds(resourceId)) {
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