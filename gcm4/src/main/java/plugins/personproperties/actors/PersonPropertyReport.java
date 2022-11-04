package plugins.personproperties.actors;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
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
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.util.properties.PropertyError;
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

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportId reportId;
		private ReportPeriod reportPeriod;
		private Set<PersonPropertyId> includedProperties = new LinkedHashSet<>();
		private Set<PersonPropertyId> excludedProperties = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy;
	}

	/**
	 * Returns a new instance of the builder class
	 */
	public Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for the report
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public PersonPropertyReport build() {
			try {
				return new PersonPropertyReport(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the default policy for inclusion of person properties in the
		 * report. This policy is used when a person property has not been
		 * explicitly included or excluded. Defaulted to false.
		 */
		public Builder setDefaultInclusion(boolean include) {
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given person property id to be included in the report.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 */
		public Builder includePersonProperty(PersonPropertyId personPropertyId) {
			if (personPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedProperties.add(personPropertyId);
			data.excludedProperties.remove(personPropertyId);
			return this;
		}

		/**
		 * Selects the given person property id to be excluded from the report
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 */
		public Builder excludePersonProperty(PersonPropertyId personPropertyId) {
			if (personPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedProperties.remove(personPropertyId);
			data.excludedProperties.add(personPropertyId);
			return this;
		}

		/**
		 * Sets the report id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the report
		 *             id is null</li>
		 */
		public Builder setReportId(ReportId reportId) {
			if (reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}
			data.reportId = reportId;
			return this;
		}

		/**
		 * Sets the report period id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if the
		 *             report period is null</li>
		 */
		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			if (reportPeriod == null) {
				throw new ContractException(ReportError.NULL_REPORT_PERIOD);
			}
			data.reportPeriod = reportPeriod;
			return this;
		}

	}

	private final Data data;

	private PersonPropertyReport(Data data) {
		super(data.reportId, data.reportPeriod);
		this.data = data;
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
		for (final PersonPropertyId personPropertyId : includedPersonPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonPropertyUpdateEvent(ActorContext context, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonPropertyId personPropertyId = personPropertyUpdateEvent.getPersonPropertyId();
		if (includedPersonPropertyIds.contains(personPropertyId)) {
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
		for (PersonPropertyId personPropertyId : includedPersonPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			decrement(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonRegionUpdateEvent(ActorContext context, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.getPersonId();
		RegionId previousRegionId = personRegionUpdateEvent.getPreviousRegionId();
		RegionId regionId = personRegionUpdateEvent.getCurrentRegionId();
		for (final PersonPropertyId personPropertyId : includedPersonPropertyIds) {
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

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);

		subscribe(peopleDataManager.getEventFilterForPersonAdditionEvent(), this::handlePersonAdditionEvent);
		subscribe(peopleDataManager.getEventFilterForPersonImminentRemovalEvent(), this::handlePersonImminentRemovalEvent);
		subscribe(regionsDataManager.getEventFilterForPersonRegionUpdateEvent(), this::handlePersonRegionUpdateEvent);

		includedPersonPropertyIds.addAll(data.includedProperties);
		excludedPersonPropertyIds.addAll(data.excludedProperties);
		if (data.defaultInclusionPolicy) {
			includedPersonPropertyIds.addAll(personPropertiesDataManager.getPersonPropertyIds());
			includedPersonPropertyIds.removeAll(excludedPersonPropertyIds);
			subscribe(personPropertiesDataManager.getEventFilterForPersonPropertyDefinitionEvent(), this::handlePersonPropertyDefinitionEvent);
		}

		subscribe(personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(), this::handlePersonPropertyUpdateEvent);

		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			for (final PersonPropertyId personPropertyId : includedPersonPropertyIds) {
				if (personPropertiesDataManager.personPropertyIdExists(personPropertyId)) {
					final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
					increment(regionId, personPropertyId, personPropertyValue);
				}
			}
		}
	}

	private void handlePersonPropertyDefinitionEvent(ActorContext actorContext, PersonPropertyDefinitionEvent personPropertyDefinitionEvent) {
		PersonPropertyId personPropertyId = personPropertyDefinitionEvent.getPersonPropertyId();
		if (!excludedPersonPropertyIds.contains(personPropertyId)) {
			includedPersonPropertyIds.add(personPropertyId);
		}
	}

}