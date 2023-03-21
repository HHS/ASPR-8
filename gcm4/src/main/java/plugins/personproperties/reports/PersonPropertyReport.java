package plugins.personproperties.reports;

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
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyDefinitionEvent;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A periodic Report that displays the number of people exhibiting a particular
 * value for each person property for a given region pair. Only non-zero person
 * counts are reported.
 *
 *
 * Fields
 *
 * region -- the region identifier
 *
 * property -- the person property identifier
 *
 * value -- the value of the property
 *
 * person_count -- the number of people having the property value within the
 * region
 *
 *
 */
public final class PersonPropertyReport extends PeriodicReport {

	private final boolean includeNewProperties;

	public PersonPropertyReport(PersonPropertyReportPluginData personPropertyReportPluginData) {
		super(personPropertyReportPluginData.getReportLabel(), personPropertyReportPluginData.getReportPeriod());
		includedPersonPropertyIds.addAll(personPropertyReportPluginData.getIncludedProperties());
		excludedPersonPropertyIds.addAll(personPropertyReportPluginData.getExcludedProperties());
		includeNewProperties = personPropertyReportPluginData.getDefaultInclusionPolicy();
	}

	/*
	 * A counter for people having the tuple (Region, Person Property, Property
	 * Value)
	 */
	private final static class Counter {
		int count;
	}

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<PersonPropertyId> includedPersonPropertyIds = new LinkedHashSet<>();
	private final Set<PersonPropertyId> currentProperties = new LinkedHashSet<>();
	private final Set<PersonPropertyId> excludedPersonPropertyIds = new LinkedHashSet<>();

	/*
	 * The tuple mapping to person counts that is maintained via handling of
	 * events.
	 */
	private final Map<RegionId, Map<PersonPropertyId, Map<Object, Counter>>> tupleMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
																	.add("region")//
																	.add("property")//
																	.add("value")//
																	.add("person_count")//
																	.build();//
		}
		return reportHeader;
	}

	/*
	 * Decrements the population for the given tuple
	 */
	private void decrement(final RegionId regionId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		getCounter(regionId, personPropertyId, personPropertyValue).count--;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		/*
		 * For each tuple having a positive population, report the tuple
		 */
		for (final RegionId regionId : tupleMap.keySet()) {
			final Map<PersonPropertyId, Map<Object, Counter>> propertyIdMap = tupleMap.get(regionId);
			for (final PersonPropertyId personPropertyId : propertyIdMap.keySet()) {
				final Map<Object, Counter> personPropertyValueMap = propertyIdMap.get(personPropertyId);
				for (final Object personPropertyValue : personPropertyValueMap.keySet()) {
					final Counter counter = personPropertyValueMap.get(personPropertyValue);
					if (counter.count > 0) {
						final int personCount = counter.count;
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportLabel(getReportLabel());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(regionId.toString());
						reportItemBuilder.addValue(personPropertyId.toString());
						reportItemBuilder.addValue(personPropertyValue);
						reportItemBuilder.addValue(personCount);

						reportContext.releaseOutput(reportItemBuilder.build());
					}
				}
			}

		}
	}

	/*
	 * Returns the counter for the give tuple. Creates the counter if it does
	 * not already exist.
	 */
	private Counter getCounter(final RegionId regionId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		Map<PersonPropertyId, Map<Object, Counter>> propertyIdMap = tupleMap.get(regionId);
		if (propertyIdMap == null) {
			propertyIdMap = new LinkedHashMap<>();
			tupleMap.put(regionId, propertyIdMap);
		}
		Map<Object, Counter> propertyValueMap = propertyIdMap.get(personPropertyId);
		if (propertyValueMap == null) {
			propertyValueMap = new LinkedHashMap<>();
			propertyIdMap.put(personPropertyId, propertyValueMap);
		}
		Counter counter = propertyValueMap.get(personPropertyValue);
		if (counter == null) {
			counter = new Counter();
			propertyValueMap.put(personPropertyValue, counter);
		}
		return counter;

	}

	private void handlePersonAdditionEvent(ReportContext reportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.personId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		for (final PersonPropertyId personPropertyId : currentProperties) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonPropertyUpdateEvent(ReportContext reportContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonPropertyId personPropertyId = personPropertyUpdateEvent.personPropertyId();
		if (isCurrentProperty(personPropertyId)) {
			PersonId personId = personPropertyUpdateEvent.personId();
			Object previousPropertyValue = personPropertyUpdateEvent.previousPropertyValue();
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			final Object currentValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, currentValue);
			decrement(regionId, personPropertyId, previousPropertyValue);
		}
	}

	private void handlePersonImminentRemovalEvent(ReportContext reportContext, PersonImminentRemovalEvent personImminentRemovalEvent) {
		PersonId personId = personImminentRemovalEvent.personId();
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		for (PersonPropertyId personPropertyId : currentProperties) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			decrement(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonRegionUpdateEvent(ReportContext reportContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId regionId = personRegionUpdateEvent.currentRegionId();
		for (final PersonPropertyId personPropertyId : currentProperties) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
			decrement(previousRegionId, personPropertyId, personPropertyValue);
		}
	}

	/*
	 * Increments the population for the given tuple
	 */
	private void increment(final RegionId regionId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		getCounter(regionId, personPropertyId, personPropertyValue).count++;
	}

	private PersonPropertiesDataManager personPropertiesDataManager;

	private RegionsDataManager regionsDataManager;

	private PeopleDataManager peopleDataManager;

	private boolean isCurrentProperty(PersonPropertyId personPropertyId) {
		return currentProperties.contains(personPropertyId);
	}

	private boolean addToCurrentProperties(PersonPropertyId personPropertyId) {

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
		if (excludedPersonPropertyIds.contains(personPropertyId)) {
			return false;
		}

		// if both P and I are false we don't add the property
		boolean included = includedPersonPropertyIds.contains(personPropertyId);

		if (!included && !includeNewProperties) {
			return false;
		}

		// we have failed to reject the property
		currentProperties.add(personPropertyId);

		return true;
	}

	@Override
	protected void prepare(final ReportContext reportContext) {
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);

		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		reportContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		reportContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
		reportContext.subscribeToSimulationState(this::recordSimulationState);
		reportContext.subscribe(PersonPropertyDefinitionEvent.class, this::handlePersonPropertyDefinitionEvent);
		reportContext.subscribe(PersonPropertyUpdateEvent.class, this::handlePersonPropertyUpdateEvent);

		for (PersonPropertyId personPropertyId : personPropertiesDataManager.getPersonPropertyIds()) {
			addToCurrentProperties(personPropertyId);
		}

		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (final PersonPropertyId personPropertyId : currentProperties) {
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId, personPropertyId, personPropertyValue);
			}
		}
	}

	private void recordSimulationState(ReportContext reportContext, SimulationStateContext simulationStateContext) {
		PersonPropertyReportPluginData.Builder builder = simulationStateContext.get(PersonPropertyReportPluginData.Builder.class);
		builder.setDefaultInclusion(includeNewProperties);
		builder.setReportLabel(getReportLabel());
		builder.setReportPeriod(getReportPeriod());
		for (PersonPropertyId personPropertyId : includedPersonPropertyIds) {
			builder.includePersonProperty(personPropertyId);
		}
		for (PersonPropertyId personPropertyId : excludedPersonPropertyIds) {
			builder.excludePersonProperty(personPropertyId);
		}
	}

	private void handlePersonPropertyDefinitionEvent(ReportContext actorContext, PersonPropertyDefinitionEvent personPropertyDefinitionEvent) {
		PersonPropertyId personPropertyId = personPropertyDefinitionEvent.personPropertyId();
		if (addToCurrentProperties(personPropertyId)) {			
			for (PersonId personId : peopleDataManager.getPeople()) {
				final RegionId regionId = regionsDataManager.getPersonRegion(personId);
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId, personPropertyId, personPropertyValue);
			}
		}
	}

}