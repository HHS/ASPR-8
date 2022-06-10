package plugins.personproperties.actors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.errors.ContractException;

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

	private void handlePersonAdditionEvent(ActorContext context, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		for (final PersonPropertyId personPropertyId : personPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handleBulkPersonAdditionEvent(ActorContext context, BulkPersonAdditionEvent bulkPersonAdditionEvent) {
		for (PersonId personId : bulkPersonAdditionEvent.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (final PersonPropertyId personPropertyId : personPropertyIds) {
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId, personPropertyId, personPropertyValue);
			}
		}
	}

	private void handlePersonPropertyUpdateEvent(ActorContext context, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonPropertyId personPropertyId = personPropertyUpdateEvent.getPersonPropertyId();
		if (personPropertyIds.contains(personPropertyId)) {
			PersonId personId = personPropertyUpdateEvent.getPersonId();
			Object previousPropertyValue = personPropertyUpdateEvent.getPreviousPropertyValue();
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			final Object currentValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, currentValue);
			decrement(regionId, personPropertyId, previousPropertyValue);
		}
	}

	private void handlePersonImminentRemovalEvent(ActorContext context, PersonImminentRemovalEvent personImminentRemovalEvent) {
		PersonId personId = personImminentRemovalEvent.getPersonId();
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			decrement(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonRegionUpdateEvent(ActorContext context, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.getPersonId();
		RegionId previousRegionId = personRegionUpdateEvent.getPreviousRegionId();
		RegionId regionId = personRegionUpdateEvent.getCurrentRegionId();
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

	private RegionsDataManager regionsDataManager;

	/**
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID}
	 *             if a person property specified in construction is
	 *             unknown</li>
	 */
	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		actorContext.subscribe(PersonAdditionEvent.class, getFlushingConsumer(this::handlePersonAdditionEvent));
		actorContext.subscribe(BulkPersonAdditionEvent.class, getFlushingConsumer(this::handleBulkPersonAdditionEvent));
		
		actorContext.subscribe(PersonImminentRemovalEvent.class, getFlushingConsumer(this::handlePersonImminentRemovalEvent));
		actorContext.subscribe(PersonRegionUpdateEvent.class, getFlushingConsumer(this::handlePersonRegionUpdateEvent));

		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		/*
		 * If no person properties were specified, then assume all are wanted
		 */
		if (personPropertyIds.isEmpty()) {
			personPropertyIds.addAll(personPropertiesDataManager.getPersonPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<PersonPropertyId> validPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
		for (final PersonPropertyId personPropertyId : personPropertyIds) {
			if (!validPropertyIds.contains(personPropertyId)) {
				throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
			}
		}

		// If all person properties are included, then subscribe to the event
		// class, otherwise subscribe to the individual property values
		if (personPropertyIds.equals(personPropertiesDataManager.getPersonPropertyIds())) {
			actorContext.subscribe(PersonPropertyUpdateEvent.class, getFlushingConsumer(this::handlePersonPropertyUpdateEvent));
			// since we are subscribing to all person properties, we must
			// subscribe to the PersonPropertyDefinitionEvent as well
			actorContext.subscribe(PersonPropertyDefinitionEvent.class, getFlushingConsumer(this::handlePersonPropertyDefinitionEvent));
		} else {
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				EventLabel<PersonPropertyUpdateEvent> eventLabelByProperty = PersonPropertyUpdateEvent.getEventLabelByProperty(actorContext, personPropertyId);
				actorContext.subscribe(eventLabelByProperty, getFlushingConsumer(this::handlePersonPropertyUpdateEvent));
			}
		}

		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (final PersonPropertyId personPropertyId : personPropertyIds) {
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId, personPropertyId, personPropertyValue);
			}
		}

	}

	private void handlePersonPropertyDefinitionEvent(ActorContext actorContext, PersonPropertyDefinitionEvent personPropertyDefinitionEvent) {

		PersonPropertyId personPropertyId = personPropertyDefinitionEvent.getPersonPropertyId();
		personPropertyIds.add(personPropertyId);
	}

}