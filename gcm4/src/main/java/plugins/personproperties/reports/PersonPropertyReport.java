package plugins.personproperties.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ReportContext;
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
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
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
 *
 */
public final class PersonPropertyReport extends PeriodicReport {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;
		private Set<PersonPropertyId> includedProperties = new LinkedHashSet<>();
		private Set<PersonPropertyId> excludedProperties = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;
	}

	/**
	 * Returns a new instance of the builder class
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for the report
	 * 
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
		 * explicitly included or excluded. Defaulted to true.
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
		 * Sets the report label
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_LABEL} if the report
		 *             label is null</li>
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
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
		super(data.reportLabel, data.reportPeriod);
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
					//if (counter.count > 0) {
						final int personCount = counter.count;
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportLabel(getReportLabel());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(regionId.toString());
						reportItemBuilder.addValue(personPropertyId.toString());
						reportItemBuilder.addValue(personPropertyValue);
						reportItemBuilder.addValue(personCount);

						reportContext.releaseOutput(reportItemBuilder.build());
					//}
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
		for (final PersonPropertyId personPropertyId : includedPersonPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			increment(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonPropertyUpdateEvent(ReportContext reportContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		PersonPropertyId personPropertyId = personPropertyUpdateEvent.personPropertyId();
		if (includedPersonPropertyIds.contains(personPropertyId)) {
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
		for (PersonPropertyId personPropertyId : includedPersonPropertyIds) {
			final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			decrement(regionId, personPropertyId, personPropertyValue);
		}
	}

	private void handlePersonRegionUpdateEvent(ReportContext reportContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		PersonId personId = personRegionUpdateEvent.personId();
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId regionId = personRegionUpdateEvent.currentRegionId();
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

	private PeopleDataManager peopleDataManager;

	@Override
	public void init(final ReportContext reportContext) {
		super.init(reportContext);

		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		personPropertiesDataManager = reportContext.getDataManager(PersonPropertiesDataManager.class);
		peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);

		subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);
		subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);

		includedPersonPropertyIds.addAll(data.includedProperties);
		excludedPersonPropertyIds.addAll(data.excludedProperties);
		if (data.defaultInclusionPolicy) {
			includedPersonPropertyIds.addAll(personPropertiesDataManager.getPersonPropertyIds());
			includedPersonPropertyIds.removeAll(excludedPersonPropertyIds);
			subscribe(PersonPropertyDefinitionEvent.class, this::handlePersonPropertyDefinitionEvent);
		}

		subscribe(PersonPropertyUpdateEvent.class, this::handlePersonPropertyUpdateEvent);

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

	private void handlePersonPropertyDefinitionEvent(ReportContext actorContext, PersonPropertyDefinitionEvent personPropertyDefinitionEvent) {
		PersonPropertyId personPropertyId = personPropertyDefinitionEvent.personPropertyId();
		if (!excludedPersonPropertyIds.contains(personPropertyId)) {
			includedPersonPropertyIds.add(personPropertyId);
			for (PersonId personId : peopleDataManager.getPeople()) {
				final RegionId regionId = regionsDataManager.getPersonRegion(personId);
				final Object personPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				increment(regionId, personPropertyId, personPropertyValue);
			}
		}
	}

}