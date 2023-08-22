package gov.hhs.aspr.ms.gcm.plugins.resources.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.events.PersonRegionUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.regions.events.RegionAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.PersonResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourceIdAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import util.errors.ContractException;
import util.wrappers.MutableInteger;

/**
 * A periodic Report that displays number of people who have/do not have any
 * units of a particular resource with a region. Fields region -- the region
 * identifier resource -- the resource identifier people_with_resource -- the
 * number of people in the region who have at least one unit of the given
 * resource people_without_resource -- the number of people in the region pair
 * who do not have any units of the given resource
 */
public final class PersonResourceReport extends PeriodicReport {
	public PersonResourceReport(PersonResourceReportPluginData personResourceReportPluginData) {
		super(personResourceReportPluginData.getReportLabel(), personResourceReportPluginData.getReportPeriod());
		this.includedResourceIds.addAll(personResourceReportPluginData.getIncludedResourceIds());
		this.excludedResourceIds.addAll(personResourceReportPluginData.getExcludedResourceIds());
		this.includeNewResourceIds = personResourceReportPluginData.getDefaultInclusionPolicy();
	}

	/*
	 * The resources that will be used in this report. They are derived from the
	 * values passed in the init() method.
	 */
	private final boolean includeNewResourceIds;
	private final Set<ResourceId> includedResourceIds = new LinkedHashSet<>();
	private final Set<ResourceId> currentResourceIds = new LinkedHashSet<>();
	private final Set<ResourceId> excludedResourceIds = new LinkedHashSet<>();

	// Mapping of the (regionId, resource Id, InventoryType) to
	// sets of person id. Maintained via the processing of events.
	private final Map<RegionId, Map<ResourceId, MutableInteger>> regionMap = new LinkedHashMap<>();

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
			reportHeaderBuilder.add("people_without_resource");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	/*
	 * Adds a person to the set of people associated with the given tuple
	 */
	private void inc(final RegionId regionId, final ResourceId resourceId) {
		get(regionId, resourceId).increment();
	}

	private void dec(final RegionId regionId, final ResourceId resourceId) {
		get(regionId, resourceId).decrement();
	}

	private MutableInteger get(final RegionId regionId, final ResourceId resourceId) {
		Map<ResourceId, MutableInteger> map = regionMap.get(regionId);
		if (map == null) {
			map = new LinkedHashMap<>();
			regionMap.put(regionId, map);
		}
		MutableInteger mutableInteger = map.get(resourceId);

		if (mutableInteger == null) {
			mutableInteger = new MutableInteger();
			map.put(resourceId, mutableInteger);
		}
		return mutableInteger;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		for (final RegionId regionId : regionMap.keySet()) {
			int populationCount = regionsDataManager.getRegionPopulationCount(regionId);
			Map<ResourceId, MutableInteger> resourceMap = regionMap.get(regionId);
			for (final ResourceId resourceId : resourceMap.keySet()) {
				MutableInteger mutableInteger = resourceMap.get(resourceId);
				ReportItem.Builder reportItemBuilder = ReportItem.builder();
				reportItemBuilder.setReportHeader(getReportHeader());
				reportItemBuilder.setReportLabel(getReportLabel());
				fillTimeFields(reportItemBuilder);
				reportItemBuilder.addValue(regionId.toString());
				reportItemBuilder.addValue(resourceId.toString());
				reportItemBuilder.addValue(mutableInteger.getValue());
				reportItemBuilder.addValue(populationCount - mutableInteger.getValue());
				reportContext.releaseOutput(reportItemBuilder.build());
			}

		}
	}

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.personId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (final ResourceId resourceId : currentResourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				inc(regionId, resourceId);
			}
		}
	}

	private void handlePersonImminentRemovalEvent(ReportContext reportContext,
			PersonImminentRemovalEvent personImminentRemovalEvent) {

		PersonId personId = personImminentRemovalEvent.personId();

		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		for (ResourceId resourceId : currentResourceIds) {
			Long amount = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (amount > 0) {
				dec(regionId, resourceId);
			}
		}
	}

	private void handlePersonResourceUpdateEvent(ReportContext reportContext,
			PersonResourceUpdateEvent personResourceUpdateEvent) {
		ResourceId resourceId = personResourceUpdateEvent.resourceId();
		if (isCurrentProperty(resourceId)) {
			PersonId personId = personResourceUpdateEvent.personId();
			RegionId regionId = regionsDataManager.getPersonRegion(personId);
			long currentLevel = personResourceUpdateEvent.currentResourceLevel();
			long previousLevel = personResourceUpdateEvent.previousResourceLevel();
			if (previousLevel > 0 && currentLevel == 0) {
				dec(regionId, resourceId);
			} else if (previousLevel == 0 && currentLevel > 0) {
				inc(regionId, resourceId);
			}
		}
	}

	private void handlePersonRegionUpdateEvent(ReportContext reportContext,
			PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.currentRegionId();

		for (final ResourceId resourceId : currentResourceIds) {
			final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
			if (personResourceLevel > 0) {
				dec(previousRegionId, resourceId);
				inc(currentRegionId, resourceId);
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
		 * Two of the cases above are contradictory since a property cannot be both
		 * explicitly included and explicitly excluded
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

	private PeopleDataManager peopleDataManager;

	/**
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if a resource id passed to the constructor is
	 *                           unknown</li>
	 *                           <li></li>
	 *                           </ul>
	 */
	@Override
	protected void prepare(final ReportContext reportContext) {
		resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		reportContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		reportContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
		reportContext.subscribe(PersonResourceUpdateEvent.class, this::handlePersonResourceUpdateEvent);
		reportContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);

		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		for (final ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			addToCurrentResourceIds(resourceId);
		}

		/*
		 * Place the initial population in the mapping
		 */
		for (final PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (final ResourceId resourceId : currentResourceIds) {

				final long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				if (personResourceLevel > 0) {
					inc(regionId, resourceId);
				}
			}
		}

	}

	private void recordSimulationState(ReportContext reportContext) {
		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData.builder();
		for (ResourceId resourceId : includedResourceIds) {
			builder.includeResource(resourceId);
		}
		for (ResourceId resourceId : excludedResourceIds) {
			builder.excludeResource(resourceId);
		}
		builder.setDefaultInclusion(includeNewResourceIds);
		builder.setReportLabel(getReportLabel());
		builder.setReportPeriod(getReportPeriod());
		reportContext.releaseOutput(builder.build());
	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		if (!regionMap.containsKey(regionId)) {
			for (PersonId personId : regionsDataManager.getPeopleInRegion(regionId)) {
				for (final ResourceId resourceId : currentResourceIds) {
					long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
					if (personResourceLevel > 0) {
						inc(regionId, resourceId);
					}
				}
			}
		}
	}

	private void handleResourceIdAdditionEvent(ReportContext reportContext,
			ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		if (addToCurrentResourceIds(resourceId)) {
			for (PersonId personId : peopleDataManager.getPeople()) {
				long personResourceLevel = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
				if (personResourceLevel > 0) {
					RegionId regionId = regionsDataManager.getPersonRegion(personId);
					inc(regionId, resourceId);
				}
			}
		}
	}

}