package plugins.personproperties.actors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventLabel;
import plugins.people.PersonDataManager;
import plugins.people.events.PersonCreationObservationEvent;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyChangeObservationEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datamanagers.PersonRegionChangeObservationEvent;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of people exhibiting a particular
 * value for each person property for a given region pair. Only
 * non-zero person counts are reported.
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
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyReport extends PeriodicReport {

	public PersonPropertyReport(ReportId reportId, ReportPeriod reportPeriod, PersonPropertyId... personPropertyIds) {
		super(reportId, reportPeriod);
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			this.personPropertyIds.add(personPropertyId);
		}
	}

	/*
	 * A counter for people having the tuple (Region, Compartment, Person
	 * Property, Property Value)
	 */
	private final static class Counter {
		int count;
	}

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<PersonPropertyId> personPropertyIds = new LinkedHashSet<>();

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
	protected void flush(ActorContext actorContext) {

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
						reportItemBuilder.setReportId(getReportId());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(regionId.toString());
						reportItemBuilder.addValue(personPropertyId.toString());
						reportItemBuilder.addValue(personPropertyValue);
						reportItemBuilder.addValue(personCount);

						actorContext.releaseOutput(reportItemBuilder.build());
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
		final Map<Object, Counter> propertyValueMap = tupleMap.get(regionId).get(personPropertyId);
		Counter counter = propertyValueMap.get(personPropertyValue);
		if (counter == null) {
			counter = new Counter();
			propertyValueMap.put(personPropertyValue, counter);
		}
		return counter;

	}

	private void handlePersonCreationObservationEvent(ActorContext context, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		final RegionId regionId = regionDataManager.getPersonRegion(personId);
		for (final PersonPropertyId personPropertyId : personPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonPropertyChangeObservationEvent(ActorContext context, PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonPropertyId personPropertyId = personPropertyChangeObservationEvent.getPersonPropertyId();
		if (personPropertyIds.contains(personPropertyId)) {
			PersonId personId = personPropertyChangeObservationEvent.getPersonId();
			Object previousPropertyValue = personPropertyChangeObservationEvent.getPreviousPropertyValue();
			final RegionId regionId = regionDataManager.getPersonRegion(personId);
			final Object currentValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, currentValue);
			decrement(regionId, personPropertyId, previousPropertyValue);
		}
	}

	private void handlePersonImminentRemovalObservationEvent(ActorContext context, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		RegionId regionId = regionDataManager.getPersonRegion(personId);
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			decrement(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonRegionChangeObservationEvent(ActorContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId regionId = personRegionChangeObservationEvent.getCurrentRegionId();
		for (final PersonPropertyId personPropertyId : personPropertyIds) {
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

	private RegionDataManager regionDataManager;

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		actorContext.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		actorContext.subscribe(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);
		actorContext.subscribe(PersonRegionChangeObservationEvent.class, this::handlePersonRegionChangeObservationEvent);

		regionDataManager = actorContext.getDataManager(RegionDataManager.class).get();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class).get();
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class).get();

		/*
		 * If no person properties were specified, then assume all are wanted
		 */
		if (personPropertyIds.size() == 0) {
			personPropertyIds.addAll(personPropertiesDataManager.getPersonPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<PersonPropertyId> validPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
		for (final PersonPropertyId personPropertyId : personPropertyIds) {
			if (!validPropertyIds.contains(personPropertyId)) {
				throw new RuntimeException("invalid property id " + personPropertyId);
			}
		}

		// If all person properties are included, then subscribe to the event
		// class, otherwise subscribe to the individual property values
		if (personPropertyIds.equals(personPropertiesDataManager.getPersonPropertyIds())) {
			actorContext.subscribe(PersonPropertyChangeObservationEvent.class, this::handlePersonPropertyChangeObservationEvent);
		} else {
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				EventLabel<PersonPropertyChangeObservationEvent> eventLabelByProperty = PersonPropertyChangeObservationEvent.getEventLabelByProperty(actorContext, personPropertyId);
				actorContext.subscribe(eventLabelByProperty, this::handlePersonPropertyChangeObservationEvent);
			}
		}

		/*
		 * Fill the top layers of the regionMap. We do not yet know the set of
		 * property values, so we leave that layer empty.
		 *
		 */

		// Map<RegionId, Map<PersonPropertyId, Map<Object, Counter>>>
		final Set<RegionId> regionIds = actorContext.getDataManager(RegionDataManager.class).get().getRegionIds();
		for (final RegionId regionId : regionIds) {
			final Map<PersonPropertyId, Map<Object, Counter>> propertyIdMap = new LinkedHashMap<>();
			tupleMap.put(regionId, propertyIdMap);
			for (final PersonPropertyId personPropertyId : personPropertyIds) {
				final Map<Object, Counter> propertyValueMap = new LinkedHashMap<>();
				propertyIdMap.put(personPropertyId, propertyValueMap);
			}
		}

		for (PersonId personId : personDataManager.getPeople()) {
			final RegionId regionId = regionDataManager.getPersonRegion(personId);
			for (final PersonPropertyId personPropertyId : personPropertyIds) {
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId,  personPropertyId, personPropertyValue);
			}
		}

	}

}