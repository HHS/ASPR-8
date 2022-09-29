package plugins.personproperties.actors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nucleus.ActorContext;
import nucleus.EventLabel;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.BulkPersonAdditionEvent;
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
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of people exhibiting a tuple of
 * person property values for a given region. Only non-zero person counts are
 * reported.
 *
 *
 * Fields
 *
 * region -- the region identifier
 *
 * [propertyIds] -- the tuple of property field values
 *
 * person count -- the number of people having the tuple of property values
 * within the region pair
 *
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyInteractionReport extends PeriodicReport {

	public PersonPropertyInteractionReport(ReportId reportId, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
		super(reportId, reportPeriod);
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			propertyIds.add(personPropertyId);
		}
	}

	/*
	 * Represents a count of people in a particular region and having a
	 * particular tuple of property values.
	 */
	private static class Counter {
		int count;
	}

	private final List<PersonPropertyId> propertyIds = new ArrayList<>();

	/*
	 * Map of <Region, Map<Property Value, ... Map<Property Value,Counter>...>>
	 * 
	 * A map of map of map... that starts with regions, each property id in
	 * order and ends with Counter
	 */

	private final Map<Object, Object> regionMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	/*
	 * Returns the report header for this report having columns for the selected
	 * property id values
	 */
	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			addTimeFieldHeaders(reportHeaderBuilder).add("region");
			for (final PersonPropertyId personPropertyId : propertyIds) {
				reportHeaderBuilder.add(personPropertyId.toString().toLowerCase());
			}
			reportHeaderBuilder.add("person_Count");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	/*
	 * Decrements the Counter for the given region and person property values
	 * associated with the person
	 */
	private void decrement(final Object regionId, final PersonId personId) {
		getCounter(regionId, personId, null, null).count--;
	}

	/*
	 * Decrements the Counter for the given region and person property values
	 * associated with the person with the old property value being used instead
	 * of the current property value.
	 */
	private void decrementOldPropertyValue(final Object regionId, final PersonId personId, final Object oldPropertyId, final Object oldPropertyValue) {
		getCounter(regionId, personId, oldPropertyId, oldPropertyValue).count--;
	}

	@Override
	protected void flush(ActorContext actorContext) {

		/*
		 * For each region pair, execute the recursive propertyFlush
		 */
		final Object[] propertyValues = new Object[propertyIds.size()];
		for (final Object regionId : regionMap.keySet()) {

			@SuppressWarnings("unchecked")
			final Map<Object, Object> map = (Map<Object, Object>) regionMap.get(regionId);
			propertyFlush(actorContext, regionId, map, propertyValues, 0);

		}
	}

	/*
	 * Selects the counter that is accounting for the people in the region who
	 * have the same tuple of property values that the given person currently
	 * has. If the selectedPropertyId is not null, then the formerPropertyValue
	 * is used instead for forming the tuple. This is done to select the counter
	 * for the previous property value so that the counter may decremented.
	 */
	private Counter getCounter(final Object regionId, final PersonId personId, final Object selectedPropertyId, final Object formerPropertyValue) {

		/*
		 * First, push through the region map with the region to arrive at a
		 * nested map of maps for the properties
		 */

		@SuppressWarnings("unchecked")
		Map<Object, Object> propertyValueMap = (Map<Object, Object>) regionMap.get(regionId);
		if (propertyValueMap == null) {
			propertyValueMap = new LinkedHashMap<>();
			regionMap.put(regionId, propertyValueMap);
		}

		/*
		 * Push downward through the mapping layers until all property values
		 * have been used. The last layer will have Counters as its values.
		 */
		final int n = propertyIds.size();
		for (int i = 0; i < n; i++) {
			final PersonPropertyId personPropertyId = propertyIds.get(i);
			Object personPropertyValue;
			/*
			 * When this method is being used to decrement a counter for a
			 * previous value of a property, we select the former property value
			 * instead of the current property value.
			 */
			if (personPropertyId.equals(selectedPropertyId)) {
				personPropertyValue = formerPropertyValue;
			} else {
				personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			}

			/*
			 * The last map level has Counters as its values. All other levels
			 * will have maps as their values.
			 */
			if (i == (n - 1)) {
				Counter counter = (Counter) propertyValueMap.get(personPropertyValue);
				if (counter == null) {
					counter = new Counter();
					propertyValueMap.put(personPropertyValue, counter);
				}
				return counter;
			}
			@SuppressWarnings("unchecked")
			Map<Object, Object> subMap = (Map<Object, Object>) propertyValueMap.get(personPropertyValue);
			if (subMap == null) {
				subMap = new LinkedHashMap<>();
				propertyValueMap.put(personPropertyValue, subMap);
			}
			propertyValueMap = subMap;
		}
		return null;
	}

	private void handleBulkPersonAdditionEvent(ActorContext actorContext, BulkPersonAdditionEvent bulkPersonAdditionEvent) {
		for (PersonId personId : bulkPersonAdditionEvent.getPeople()) {
			final Object regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, personId);
		}
	}

	private void handlePersonAdditionEvent(ActorContext actorContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		final Object regionId = regionsDataManager.getPersonRegion(personId);
		increment(regionId, personId);
	}

	private void handlePersonPropertyUpdateEvent(ActorContext actorContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonPropertyId personPropertyId = personPropertyUpdateEvent.getPersonPropertyId();
		if (propertyIds.contains(personPropertyId)) {
			PersonId personId = personPropertyUpdateEvent.getPersonId();
			Object previousPropertyValue = personPropertyUpdateEvent.getPreviousPropertyValue();
			final Object regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, personId);
			decrementOldPropertyValue(regionId, personId, personPropertyId, previousPropertyValue);
		}
	}

	private void handlePersonImminentRemovalEvent(ActorContext actorContext, PersonImminentRemovalEvent personImminentRemovalEvent) {
		PersonId personId = personImminentRemovalEvent.getPersonId();
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		decrement(regionId, personId);
	}

	private void handlePersonRegionUpdateEvent(ActorContext actorContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.getPersonId();
		RegionId sourceRegionId = personRegionUpdateEvent.getPreviousRegionId();
		final Object regionId = personRegionUpdateEvent.getCurrentRegionId();
		increment(regionId, personId);
		decrement(sourceRegionId, personId);
	}

	/*
	 * Increments the Counter for the given region and person property values
	 * associated with the person
	 */
	private void increment(final Object regionId, final PersonId personId) {
		getCounter(regionId, personId, null, null).count++;
	}

	private RegionsDataManager regionsDataManager;
	private PeopleDataManager peopleDataManager;
	private PersonPropertiesDataManager personPropertiesDataManager;

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		subscribe(BulkPersonAdditionEvent.class, this::handleBulkPersonAdditionEvent);
		
		subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);

		/*
		 * if the client did not choose any properties, then we assume that all
		 * properties are selected
		 */
		if (propertyIds.size() == 0) {
			propertyIds.addAll(personPropertiesDataManager.getPersonPropertyIds());
		}

		/*
		 * Validate the client's property ids and ignore any that are not known
		 * to the environment
		 */
		final Set<PersonPropertyId> validPersonPropertyIds = personPropertiesDataManager.getPersonPropertyIds();

		final Iterator<PersonPropertyId> iterator = propertyIds.iterator();
		while (iterator.hasNext()) {
			if (!validPersonPropertyIds.contains(iterator.next())) {
				iterator.remove();
			}
		}

		// If all person properties are included, then subscribe to the event
		// class, otherwise subscribe to the individual property values
		if (propertyIds.stream().collect(Collectors.toSet()).equals(personPropertiesDataManager.getPersonPropertyIds())) {
			subscribe(PersonPropertyUpdateEvent.class, this::handlePersonPropertyUpdateEvent);
			subscribe(PersonPropertyDefinitionEvent.class, this::handlePersonPropertyDefinitionEvent);
		} else {
			for (PersonPropertyId personPropertyId : propertyIds) {
				EventLabel<PersonPropertyUpdateEvent> eventLabelByProperty = PersonPropertyUpdateEvent.getEventLabelByProperty(actorContext, personPropertyId);
				subscribe(eventLabelByProperty, this::handlePersonPropertyUpdateEvent);
			}
		}

		for (PersonId personId : peopleDataManager.getPeople()) {
			final Object regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, personId);
		}
	}

	private void handlePersonPropertyDefinitionEvent(ActorContext actorContext, PersonPropertyDefinitionEvent personPropertyDefinitionEvent) {
		propertyIds.add(personPropertyDefinitionEvent.getPersonPropertyId());
	}

	/*
	 * Flushes the positive counters recursively.
	 */
	private void propertyFlush(ActorContext actorContext, final Object regionId, final Map<Object, Object> map, final Object[] personPropertyValues, final int level) {

		for (final Object personPropertyValue : map.keySet()) {
			personPropertyValues[level] = personPropertyValue;
			if (level < (propertyIds.size() - 1)) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> subMap = (Map<Object, Object>) map.get(personPropertyValue);
				propertyFlush(actorContext, regionId, subMap, personPropertyValues, level + 1);
			} else {
				final Counter counter = (Counter) map.get(personPropertyValue);

				if (counter.count > 0) {
					final Map<String, Object> propertyIdsAndValues = new LinkedHashMap<>();
					for (int i = 0; i < propertyIds.size(); i++) {
						propertyIdsAndValues.put(propertyIds.get(i).toString(), personPropertyValues[i]);
					}
					final ReportItem.Builder reportItemBuilder = ReportItem.builder();
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportId(getReportId());

					fillTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());

					for (int i = 0; i < propertyIds.size(); i++) {
						reportItemBuilder.addValue(personPropertyValues[i]);
					}
					reportItemBuilder.addValue(counter.count);

					actorContext.releaseOutput(reportItemBuilder.build());
				}
			}
		}

	}
}
