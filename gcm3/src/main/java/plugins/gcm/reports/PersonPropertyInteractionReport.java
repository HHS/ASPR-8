package plugins.gcm.reports;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import nucleus.EventLabel;
import nucleus.ReportContext;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonId;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of people exhibiting a tuple of
 * person property values for a given region/compartment pair. Only non-zero
 * person counts are reported.
 *
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * Compartment -- the compartment identifier
 *
 * [PropertyIds] -- the tuple of property field values
 *
 * PersonCount -- the number of people having the tuple of property values
 * within the region/compartment pair
 *
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyInteractionReport extends PeriodicReport {

	public PersonPropertyInteractionReport(ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
		super(reportPeriod);
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			propertyIds.add(personPropertyId);
		}
	}

	/*
	 * Represents a count of people in a particular region, particular
	 * compartment and having a particular tuple of property values.
	 */
	private static class Counter {
		int count;
	}

	private final List<PersonPropertyId> propertyIds = new ArrayList<>();

	/*
	 * Map of <Region, Map<Compartment, Map<Property Value, ... Map<Property
	 * Value,Counter>...>>>
	 * 
	 * A map of map of map... that starts with regions, compartment, each
	 * property id in order and ends with Counter
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
			addTimeFieldHeaders(reportHeaderBuilder).add("Region").add("Compartment");
			for (final PersonPropertyId personPropertyId : propertyIds) {
				reportHeaderBuilder.add(personPropertyId.toString());
			}
			reportHeaderBuilder.add("PersonCount");
			reportHeader = reportHeaderBuilder.build();
		}
		return reportHeader;
	}

	/*
	 * Decrements the Counter for the given region, compartment and person
	 * property values associated with the person
	 */
	private void decrement(final Object regionId, final CompartmentId compartmentId, final PersonId personId) {
		getCounter(regionId, compartmentId, personId, null, null).count--;
	}

	/*
	 * Decrements the Counter for the given region, compartment and person
	 * property values associated with the person with the old property value
	 * being used instead of the current property value.
	 */
	private void decrementOldPropertyValue(final Object regionId, final CompartmentId compartmentId, final PersonId personId, final Object oldPropertyId, final Object oldPropertyValue) {
		getCounter(regionId, compartmentId, personId, oldPropertyId, oldPropertyValue).count--;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		/*
		 * For each (region,compartment) pair, execute the recursive
		 * propertyFlush
		 */
		final Object[] propertyValues = new Object[propertyIds.size()];
		for (final Object regionId : regionMap.keySet()) {
			@SuppressWarnings("unchecked")
			final Map<Object, Object> compartmentMap = (Map<Object, Object>) regionMap.get(regionId);

			for (final Object compartmentId : compartmentMap.keySet()) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> map = (Map<Object, Object>) compartmentMap.get(compartmentId);
				propertyFlush(reportContext, regionId, compartmentId, map, propertyValues, 0);
			}
		}
	}

	/*
	 * Selects the counter that is accounting for the people in the compartment
	 * and region who have the same tuple of property values that the given
	 * person currently has. If the selectedPropertyId is not null, then the
	 * formerPropertyValue is used instead for forming the tuple. This is done
	 * to select the counter for the previous property value so that the counter
	 * may decremented.
	 */
	private Counter getCounter(final Object regionId, final CompartmentId compartmentId, final PersonId personId, final Object selectedPropertyId, final Object formerPropertyValue) {

		/*
		 * First, push through the region map with the region and compartment to
		 * arrive at a nested map of maps for the properties
		 */
		@SuppressWarnings("unchecked")
		Map<Object, Object> compartmentMap = (Map<Object, Object>) regionMap.get(regionId);
		if (compartmentMap == null) {
			compartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, compartmentMap);
		}

		@SuppressWarnings("unchecked")
		Map<Object, Object> propertyValueMap = (Map<Object, Object>) compartmentMap.get(compartmentId);
		if (propertyValueMap == null) {
			propertyValueMap = new LinkedHashMap<>();
			compartmentMap.put(compartmentId, propertyValueMap);
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
				personPropertyValue = personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
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

	private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId sourceCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();

		final Object regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		increment(regionId, compartmentId, personId);
		decrement(regionId, sourceCompartmentId, personId);
	}

	private void handlePersonCreationObservationEvent(ReportContext reportContext, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		final Object regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		increment(regionId, compartmentId, personId);
	}

	private void handlePersonPropertyChangeObservationEvent(ReportContext reportContext, PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonPropertyId personPropertyId = personPropertyChangeObservationEvent.getPersonPropertyId();
		if (propertyIds.contains(personPropertyId)) {
			PersonId personId = personPropertyChangeObservationEvent.getPersonId();
			Object previousPropertyValue = personPropertyChangeObservationEvent.getPreviousPropertyValue();
			final Object regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			increment(regionId, compartmentId, personId);
			decrementOldPropertyValue(regionId, compartmentId, personId, personPropertyId, previousPropertyValue);
		}
	}

	private void handlePersonImminentRemovalObservationEvent(ReportContext reportContext, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		decrement(regionId, compartmentId, personId);
	}

	private void handlePersonRegionChangeObservationEvent(ReportContext reportContext, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId sourceRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		final Object regionId = personRegionChangeObservationEvent.getCurrentRegionId();
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		increment(regionId, compartmentId, personId);
		decrement(sourceRegionId, compartmentId, personId);
	}

	/*
	 * Increments the Counter for the given region, compartment and person
	 * property values associated with the person
	 */
	private void increment(final Object regionId, final CompartmentId compartmentId, final PersonId personId) {
		getCounter(regionId, compartmentId, personId, null, null).count++;
	}

	private CompartmentLocationDataView compartmentLocationDataView;
	private RegionLocationDataView regionLocationDataView;
	private PersonDataView personDataView;
	private PersonPropertyDataView personPropertyDataView;

	@Override
	public void init(final ReportContext reportContext) {
		super.init(reportContext);

		
		reportContext.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		reportContext.subscribe(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);
		reportContext.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);
		reportContext.subscribe(PersonRegionChangeObservationEvent.class, this::handlePersonRegionChangeObservationEvent);

		personPropertyDataView = reportContext.getDataView(PersonPropertyDataView.class).get();
		personDataView = reportContext.getDataView(PersonDataView.class).get();
		compartmentLocationDataView = reportContext.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = reportContext.getDataView(RegionLocationDataView.class).get();

		/*
		 * if the client did not choose any properties, then we assume that all
		 * properties are selected
		 */
		if (propertyIds.size() == 0) {
			propertyIds.addAll(personPropertyDataView.getPersonPropertyIds());
		}

		/*
		 * Validate the client's property ids and ignore any that are not known
		 * to the environment
		 */
		final Set<PersonPropertyId> validPersonPropertyIds = personPropertyDataView.getPersonPropertyIds();

		final Iterator<PersonPropertyId> iterator = propertyIds.iterator();
		while (iterator.hasNext()) {
			if (!validPersonPropertyIds.contains(iterator.next())) {
				iterator.remove();
			}
		}

		// If all person properties are included, then subscribe to the event
		// class, otherwise subscribe to the individual property values		
		if (propertyIds.stream().collect(Collectors.toSet()).equals(personPropertyDataView.getPersonPropertyIds())) {
			reportContext.subscribe(PersonPropertyChangeObservationEvent.class, this::handlePersonPropertyChangeObservationEvent);
		} else {
			for (PersonPropertyId personPropertyId : propertyIds) {
				EventLabel<PersonPropertyChangeObservationEvent> eventLabelByProperty = PersonPropertyChangeObservationEvent.getEventLabelByProperty(reportContext, personPropertyId);
				reportContext.subscribe(eventLabelByProperty, this::handlePersonPropertyChangeObservationEvent);
			}
		}

		for (PersonId personId : personDataView.getPeople()) {
			final Object regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			increment(regionId, compartmentId, personId);
		}
	}

	/*
	 * Flushes the positive counters recursively.
	 */
	private void propertyFlush(ReportContext reportContext, final Object regionId, final Object compartmentId, final Map<Object, Object> map, final Object[] personPropertyValues, final int level) {

		for (final Object personPropertyValue : map.keySet()) {
			personPropertyValues[level] = personPropertyValue;
			if (level < (propertyIds.size() - 1)) {
				@SuppressWarnings("unchecked")
				final Map<Object, Object> subMap = (Map<Object, Object>) map.get(personPropertyValue);
				propertyFlush(reportContext, regionId, compartmentId, subMap, personPropertyValues, level + 1);
			} else {
				final Counter counter = (Counter) map.get(personPropertyValue);

				if (counter.count > 0) {
					final Map<String, Object> propertyIdsAndValues = new LinkedHashMap<>();
					for (int i = 0; i < propertyIds.size(); i++) {
						propertyIdsAndValues.put(propertyIds.get(i).toString(), personPropertyValues[i]);
					}
					final ReportItem.Builder reportItemBuilder = ReportItem.builder();
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportId(reportContext.getCurrentReportId());

					fillTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());
					reportItemBuilder.addValue(compartmentId.toString());
					for (int i = 0; i < propertyIds.size(); i++) {
						reportItemBuilder.addValue(personPropertyValues[i]);
					}
					reportItemBuilder.addValue(counter.count);

					reportContext.releaseOutput(reportItemBuilder.build());
				}
			}
		}

	}
}
