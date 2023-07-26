package gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.IdentifiableFunctionMap;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonImminentAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.events.PersonPropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyError;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyValueInitialization;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.BooleanPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.DoublePropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.EnumPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.FloatPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.IndexedPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.IntPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.ObjectPropertyManager;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.DoubleValueContainer;
import util.errors.ContractException;

/**
 * Mutable data manager for person properties
 *
 *
 */

public final class PersonPropertiesDataManager extends DataManager {
	private static enum EventFunctionId {
		PERSON_PROPERTY_ID, //
		REGION_ID, //
		PERSON_ID, CURRENT_VALUE, PREVIOUS_VALUE;//
	}

	private static record PersonPropertyDefinitionMutationEvent(
			PersonPropertyDefinitionInitialization propertyDefinitionInitialization) implements Event {
	}

	private static record PersonPropertyUpdateMutationEvent(PersonId personId, PersonPropertyId personPropertyId,
			Object personPropertyValue) implements Event {
	}

	private static void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private Map<PersonPropertyId, PropertyDefinition> propertyDefinitions;

	private Map<PersonPropertyId, Double> propertyDefinitionTimes;

	private final Map<PersonPropertyId, IndexedPropertyManager> propertyValues = new LinkedHashMap<>();

	private Map<PersonPropertyId, Boolean> propertyTrackingPolicies;

	private final Map<PersonPropertyId, DoubleValueContainer> propertyTimes = new LinkedHashMap<>();

	private final Map<PersonPropertyId, Integer> nonDefaultBearingPropertyIds = new LinkedHashMap<>();

	private boolean[] nonDefaultChecks = new boolean[0];

	private PeopleDataManager peopleDataManager;

	private RegionsDataManager regionsDataManager;

	private DataManagerContext dataManagerContext;
	private final PersonPropertiesPluginData personPropertiesPluginData;

	/*
	 * We keep the person records in a list rather than a map so that we can
	 * retrieve a person record by index (personId).
	 */

	private IdentifiableFunctionMap<PersonPropertyUpdateEvent> functionMap = //
			IdentifiableFunctionMap.builder(PersonPropertyUpdateEvent.class)//
					.put(EventFunctionId.PERSON_PROPERTY_ID, e -> e.personPropertyId())//
					.put(EventFunctionId.REGION_ID, e -> regionsDataManager.getPersonRegion(e.personId()))//
					.put(EventFunctionId.PERSON_ID, e -> e.personId())//
					.put(EventFunctionId.CURRENT_VALUE, e -> e.currentPropertyValue())
					.put(EventFunctionId.PREVIOUS_VALUE, e -> e.previousPropertyValue()).build();//

	/**
	 * Constructs the person property data manager from the given plugin data
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_PLUGN_DATA}
	 *                           if the plugin data is null</li>
	 */
	public PersonPropertiesDataManager(PersonPropertiesPluginData personPropertiesPluginData) {
		if (personPropertiesPluginData == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA);
		}
		this.personPropertiesPluginData = personPropertiesPluginData;
	}

	private void addNonDefaultProperty(PersonPropertyId personPropertyId) {
		nonDefaultBearingPropertyIds.put(personPropertyId, nonDefaultBearingPropertyIds.size());
		nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];
	}

	private void clearNonDefaultChecks() {
		for (int i = 0; i < nonDefaultChecks.length; i++) {
			nonDefaultChecks[i] = false;
		}
	}

	/**
	 *
	 * Defines a new person property
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION_INITIALIZATION}
	 *                           if the property definition initialization is
	 *                           null</li>
	 *
	 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *                           if the person property already exists</li>
	 *
	 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *                           if the property definition has no default value and
	 *                           there is no included value assignment for some
	 *                           extant person</li>
	 */
	public void definePersonProperty(PersonPropertyDefinitionInitialization propertyDefinitionInitialization) {
		dataManagerContext
				.releaseMutationEvent(new PersonPropertyDefinitionMutationEvent(propertyDefinitionInitialization));
	}

	/**
	 * Expands the capacity of data structures to hold people by the given count.
	 * Used to more efficiently prepare for multiple population additions.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION}
	 *                           if the count is negative</li>
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {
			for (final PersonPropertyId personPropertyId : propertyValues.keySet()) {
				IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);
				indexedPropertyManager.incrementCapacity(count);

				DoubleValueContainer doubleValueContainer = propertyTimes.get(personPropertyId);
				if (doubleValueContainer != null) {
					doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
				}
			}
		}
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyDefinitionEvent} events. Matches all such events.
	 *
	 */
	public EventFilter<PersonPropertyDefinitionEvent> getEventFilterForPersonPropertyDefinitionEvent() {
		return EventFilter.builder(PersonPropertyDefinitionEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches all such events.
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent() {
		return EventFilter.builder(PersonPropertyUpdateEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id and
	 * person id.
	 *
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is not known</li>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is not known</li>
	 *
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(PersonId personId,
			PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		validatePersonExists(personId);
		return EventFilter.builder(PersonPropertyUpdateEvent.class)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_ID), personId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id.
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is not known</li>
	 *
	 *
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(
			PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return EventFilter.builder(PersonPropertyUpdateEvent.class)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
				.build();

	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id and
	 * property value.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is not known</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the person property value is null</li>
	 *
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(
			PersonPropertyId personPropertyId, Object propertyValue, boolean useCurrentValue) {
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyValueNotNull(propertyValue);
		EventFunctionId propertyValueEnum = useCurrentValue ? EventFunctionId.CURRENT_VALUE
				: EventFunctionId.PREVIOUS_VALUE;
		return EventFilter.builder(PersonPropertyUpdateEvent.class)
				.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
				.addFunctionValuePair(functionMap.get(propertyValueEnum), propertyValue).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on region id and person
	 * property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is not known</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is not known</li>
	 *
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(RegionId regionId,
			PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		validateRegionId(regionId);
		return EventFilter.builder(PersonPropertyUpdateEvent.class)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.REGION_ID), regionId)//
				.build();
	}

	private IndexedPropertyManager getIndexedPropertyManager(final PropertyDefinition propertyDefinition) {

		Supplier<Iterator<Integer>> indexIteratorSupplier = peopleDataManager::getPersonIndexIterator;

		IndexedPropertyManager indexedPropertyManager;
		if (propertyDefinition.getType() == Boolean.class) {
			indexedPropertyManager = new BooleanPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Float.class) {
			indexedPropertyManager = new FloatPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Double.class) {
			indexedPropertyManager = new DoublePropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Byte.class) {
			indexedPropertyManager = new IntPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Short.class) {
			indexedPropertyManager = new IntPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Integer.class) {
			indexedPropertyManager = new IntPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (propertyDefinition.getType() == Long.class) {
			indexedPropertyManager = new IntPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else if (Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			indexedPropertyManager = new EnumPropertyManager(propertyDefinition, indexIteratorSupplier);
		} else {
			indexedPropertyManager = new ObjectPropertyManager(propertyDefinition, indexIteratorSupplier);
		}
		return indexedPropertyManager;
	}

	/**
	 * Returns the list(no duplicates) people who have the given person property
	 * value.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *                           if the person property value is null</li>
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the person property value is not compatible with
	 *                           the property definition associated with the given
	 *                           person property id</li>
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);

		List<PersonId> result;

		int count = 0;

		final IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can size the
		 * resulting ArrayList properly.
		 */

		if (indexedPropertyManager != null) {
			final int n = peopleDataManager.getPersonIdLimit();

			for (int personIndex = 0; personIndex < n; personIndex++) {
				if (peopleDataManager.personIndexExists(personIndex)) {
					final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
					final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
					if (propertyValue.equals(personPropertyValue)) {
						count++;
					}
				}
			}

			/*
			 * Now we fill the list.
			 */
			result = new ArrayList<>(count);

			for (int personIndex = 0; personIndex < n; personIndex++) {
				if (peopleDataManager.personIndexExists(personIndex)) {
					final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
					final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
					if (propertyValue.equals(personPropertyValue)) {
						result.add(personId);
					}
				}
			}
		} else {
			result = new ArrayList<>();
		}
		return result;

	}

	/**
	 * Returns the number of people who have the given person property value.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *                           if the person property value is null</li>
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the person property value is not compatible with
	 *                           the property definition associated with the given
	 *                           person property id</li>
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {

		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can size the
		 * resulting ArrayList properly.
		 */

		int count = 0;

		final IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);

		if (indexedPropertyManager != null) {
			final int n = peopleDataManager.getPersonIdLimit();

			for (int personIndex = 0; personIndex < n; personIndex++) {
				if (peopleDataManager.personIndexExists(personIndex)) {
					final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
					final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
					if (propertyValue.equals(personPropertyValue)) {
						count++;
					}
				}
			}
		}
		return count;

	}

	/**
	 * Returns the property definition for the given person property id
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return propertyDefinitions.get(personPropertyId);
	}

	/**
	 * Returns the person property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {

		final Set<T> result = new LinkedHashSet<>(propertyDefinitions.keySet().size());
		for (final PersonPropertyId personPropertyId : propertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}

		return result;
	}

	/**
	 * Returns the time when the person's property was last assigned or zero if the
	 * value has never been assigned.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *                           <li>{@linkplain PersonPropertyError#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *                           if the person property does not have time tracking
	 *                           turned on in the associated property
	 *                           definition</li>
	 *
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyAssignmentTimesTracked(personPropertyId);
		return propertyTimes.get(personPropertyId).getValue(personId.getValue());
	}

	/**
	 * Returns true if and only if the property assignment times are being tracked
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 */
	public boolean isPropertyTimeTracked(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return propertyTrackingPolicies.get(personPropertyId);
	}

	/**
	 * Returns the default property definition time
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 */
	public double getPropertyDefinitionTime(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return propertyDefinitionTimes.get(personPropertyId);
	}

	/**
	 * Returns the current value of the person's property
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the person property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the person property id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);
		if (indexedPropertyManager != null) {
			return indexedPropertyManager.getPropertyValue(personId.getValue());
		}
		PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
		return (T) propertyDefinition.getDefaultValue().get();
	}

	private void handlePersonImminentAdditionEvent(final DataManagerContext dataManagerContext,
			final PersonImminentAdditionEvent personImminentAdditionEvent) {

		PersonConstructionData personConstructionData = personImminentAdditionEvent.personConstructionData();

		PersonId personId = personImminentAdditionEvent.personId();

		List<PersonPropertyValueInitialization> personPropertyAssignments = personConstructionData
				.getValues(PersonPropertyValueInitialization.class);

		for (final PersonPropertyValueInitialization personPropertyAssignment : personPropertyAssignments) {
			PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
			final Object personPropertyValue = personPropertyAssignment.getValue();
			validatePersonPropertyId(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			final PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		}

		if (!nonDefaultBearingPropertyIds.isEmpty()) {
			clearNonDefaultChecks();
			for (final PersonPropertyValueInitialization personPropertyAssignment : personPropertyAssignments) {
				PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
				markAssigned(personPropertyId);
			}
			verifyNonDefaultChecks();
		}

		for (final PersonPropertyValueInitialization personPropertyAssignment : personPropertyAssignments) {
			PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
			final Object personPropertyValue = personPropertyAssignment.getValue();
			int pId = personId.getValue();
			IndexedPropertyManager propertyManager = propertyValues.get(personPropertyId);
			if (propertyManager == null) {
				PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
				propertyManager = getIndexedPropertyManager(propertyDefinition);
				propertyValues.put(personPropertyId, propertyManager);
			}
			propertyManager.setPropertyValue(pId, personPropertyValue);

			Boolean trackTimes = propertyTrackingPolicies.get(personPropertyId);
			if (trackTimes) {
				DoubleValueContainer doubleValueContainer = propertyTimes.get(personPropertyId);
				if (doubleValueContainer == null) {
					Double defaultTime = propertyDefinitionTimes.get(personPropertyId);
					doubleValueContainer = new DoubleValueContainer(defaultTime,
							peopleDataManager::getPersonIndexIterator);
					propertyTimes.put(personPropertyId, doubleValueContainer);
				}
				doubleValueContainer.setValue(pId, dataManagerContext.getTime());
			}
		}

	}

	private void handlePersonImminentRemovalEvent(final DataManagerContext dataManagerContext,
			final PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.personId();

		for (final PersonPropertyId personPropertyId : propertyValues.keySet()) {
			final IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);
			indexedPropertyManager.removeId(personId.getValue());
		}
	}

	private void handlePersonPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext,
			PersonPropertyDefinitionMutationEvent personPropertyDefinitionMutationEvent) {
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = personPropertyDefinitionMutationEvent
				.propertyDefinitionInitialization();
		validatePropertyDefinitionInitializationNotNull(propertyDefinitionInitialization);
		PersonPropertyId personPropertyId = propertyDefinitionInitialization.getPersonPropertyId();
		PropertyDefinition propertyDefinition = propertyDefinitionInitialization.getPropertyDefinition();

		validatePersonPropertyIdIsUnknown(personPropertyId);
		boolean checkAllPeopleHaveValues = propertyDefinition.getDefaultValue().isEmpty();
		List<Pair<PersonId, Object>> pairs = propertyDefinitionInitialization.getPropertyValues();
		for (Pair<PersonId, Object> pair : pairs) {
			PersonId personId = pair.getFirst();
			validatePersonExists(personId);
		}

		if (checkAllPeopleHaveValues) {
			addNonDefaultProperty(personPropertyId);
			int idLimit = peopleDataManager.getPersonIdLimit();
			BitSet coverageSet = new BitSet(idLimit);

			for (Pair<PersonId, Object> pair : pairs) {
				PersonId personId = pair.getFirst();
				int pId = personId.getValue();
				coverageSet.set(pId);
			}
			for (int i = 0; i < idLimit; i++) {
				if (peopleDataManager.personIndexExists(i)) {
					boolean personCovered = coverageSet.get(i);
					if (!personCovered) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
					}
				}
			}
		}

		propertyDefinitions.put(personPropertyId, propertyDefinition);
		propertyDefinitionTimes.put(personPropertyId, dataManagerContext.getTime());
		IndexedPropertyManager propertyManager = null;
		if (!pairs.isEmpty()) {
			propertyManager = getIndexedPropertyManager(propertyDefinition);
			propertyValues.put(personPropertyId, propertyManager);
		}

		DoubleValueContainer doubleValueContainer = null;
		boolean trackTimes = propertyDefinitionInitialization.trackTimes();
		propertyTrackingPolicies.put(personPropertyId, trackTimes);
		if (trackTimes) {
			doubleValueContainer = new DoubleValueContainer(dataManagerContext.getTime(),
					peopleDataManager::getPersonIndexIterator);
			propertyTimes.put(personPropertyId, doubleValueContainer);
		}

		for (Pair<PersonId, Object> pair : pairs) {
			PersonId personId = pair.getFirst();
			int pId = personId.getValue();
			/*
			 * we do not have to validate the value since it is guaranteed to be consistent
			 * with the property definition by contract.
			 */
			Object value = pair.getSecond();
			propertyManager.setPropertyValue(pId, value);
		}

		if (dataManagerContext.subscribersExist(PersonPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonPropertyDefinitionEvent(personPropertyId));
		}

	}

	private void handlePersonPropertyUpdateMutationEvent(DataManagerContext dataManagerContext,
			PersonPropertyUpdateMutationEvent personPropertyUpdateMutationEvent) {
		PersonId personId = personPropertyUpdateMutationEvent.personId();
		PersonPropertyId personPropertyId = personPropertyUpdateMutationEvent.personPropertyId();
		Object personPropertyValue = personPropertyUpdateMutationEvent.personPropertyValue();
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		final PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		validatePropertyMutability(propertyDefinition);

		int pId = personId.getValue();
		IndexedPropertyManager propertyManager = propertyValues.get(personPropertyId);

		if (propertyManager == null) {
			propertyManager = getIndexedPropertyManager(propertyDefinition);
			propertyValues.put(personPropertyId, propertyManager);
		}

		Object oldValue = propertyManager.getPropertyValue(pId);
		propertyManager.setPropertyValue(pId, personPropertyValue);

		DoubleValueContainer doubleValueContainer = propertyTimes.get(personPropertyId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(pId, dataManagerContext.getTime());
		}

		if (dataManagerContext.subscribersExist(PersonPropertyUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(
					new PersonPropertyUpdateEvent(personId, personPropertyId, oldValue, personPropertyValue));
		}

	}

	private void loadPropertyTrackingPolicies() {
		propertyTrackingPolicies = personPropertiesPluginData.getPropertyTrackingPolicies();
	}

	private void loadPropertyDefinitions() {
		propertyDefinitions = personPropertiesPluginData.getPropertyDefinitions();
		for (final PersonPropertyId personPropertyId : propertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = personPropertiesPluginData
					.getPersonPropertyDefinition(personPropertyId);
			if (propertyDefinition.getDefaultValue().isEmpty()) {
				nonDefaultBearingPropertyIds.put(personPropertyId, nonDefaultBearingPropertyIds.size());
			}
		}
		nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];
	}

	private void loadPropertyDefinitionTimes() {
		propertyDefinitionTimes = personPropertiesPluginData.getPropertyDefinitionTimes();

		for (PersonPropertyId personPropertyId : propertyDefinitionTimes.keySet()) {
			Double defaultTime = propertyDefinitionTimes.get(personPropertyId);
			if (defaultTime > dataManagerContext.getTime()) {
				throw new ContractException(PersonPropertyError.PROPERTY_DEFAULT_TIME_EXCEEDS_SIM_TIME,
						personPropertyId);
			}
		}
	}

	private void loadPropertyValues() {
		/*
		 * There are four cases to consider:
		 *
		 * 1) person exists, property value is present -- adopt the property value
		 *
		 * 2) person exists, property value is not present -- if the default is present,
		 * there is nothing to do. Otherwise, throw an exception since there is no
		 * default value present
		 *
		 * 3) person does not exist, property value is present -- throw an exception
		 * since it appears data was collected for a non-existent person
		 *
		 * 4) person does not exist, property value is not present -- nothing to do
		 */

		Map<PersonPropertyId, List<Object>> map = personPropertiesPluginData.getPropertyValues();
		for (PersonPropertyId personPropertyId : map.keySet()) {
			List<Object> list = map.get(personPropertyId);
			PropertyDefinition propertyDefinition = propertyDefinitions.get(personPropertyId);
			final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(propertyDefinition);
			propertyValues.put(personPropertyId, indexedPropertyManager);
			boolean defaultIsPresent = propertyDefinition.getDefaultValue().isPresent();

			int n = FastMath.max(peopleDataManager.getPersonIdLimit(), list.size());
			for (int i = 0; i < n; i++) {
				if (peopleDataManager.personIndexExists(i)) {
					Object propertyValue = null;
					if (i < list.size()) {
						propertyValue = list.get(i);
					}
					if (propertyValue != null) {
						indexedPropertyManager.setPropertyValue(i, propertyValue);
					} else {
						if (!defaultIsPresent) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT,
									"person(" + i + ") lacks a property value for " + personPropertyId);
						}
					}
				} else {
					if (i < list.size()) {
						Object propertyValue = list.get(i);
						if (propertyValue != null) {
							throw new ContractException(
									PersonPropertyError.UNKNOWN_PERSON_HAS_PROPERTY_VALUE_ASSIGNMENT,
									"unknown person(" + i + ") has property value for " + personPropertyId);
						}
					}
				}
			}
		}

	}

	private void loadPropertyTimes() {

		/*
		 * There are four cases to consider:
		 *
		 * 1) person exists, time value is present -- adopt the time value
		 *
		 * 2) person exists, time value is not present -- adopt the default -- do
		 * nothing since the default is already present in the DoubleValueContainer
		 *
		 * 3) person does not exist, time value is present -- throw an exception since
		 * it appears data was collected for a non-existent person
		 *
		 * 4) person does not exist, time value is not present -- nothing to do
		 */
		Map<PersonPropertyId, List<Double>> map = personPropertiesPluginData.getPropertyTimes();
		for (PersonPropertyId personPropertyId : map.keySet()) {
			List<Double> list = map.get(personPropertyId);
			double defaultTime = propertyDefinitionTimes.get(personPropertyId);

			DoubleValueContainer doubleValueContainer = new DoubleValueContainer(defaultTime,
					peopleDataManager::getPersonIndexIterator);
			propertyTimes.put(personPropertyId, doubleValueContainer);
			int n = FastMath.max(peopleDataManager.getPersonIdLimit(), list.size());
			for (int i = 0; i < n; i++) {
				if (peopleDataManager.personIndexExists(i)) {
					if (i < list.size()) {
						Double propertyTime = list.get(i);
						if (propertyTime != null) {
							if (propertyTime > dataManagerContext.getTime()) {
								throw new ContractException(
										PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_EXCEEDS_SIM_TIME,
										"person(" + i + ") " + personPropertyId);
							}
							doubleValueContainer.setValue(i, propertyTime);
						}
					}
				} else {
					if (i < list.size()) {
						Double propertyTime = list.get(i);
						if (propertyTime != null) {
							throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_HAS_PROPERTY_ASSIGNMENT_TIME,
									"unknown person(" + i + ") has property time for " + personPropertyId);
						}
					}
				}
			}
		}
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		dataManagerContext.subscribe(PersonPropertyDefinitionMutationEvent.class,
				this::handlePersonPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(PersonPropertyUpdateMutationEvent.class,
				this::handlePersonPropertyUpdateMutationEvent);
		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonImminentAdditionEvent);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonImminentRemovalEvent);

		loadPropertyTrackingPolicies();
		loadPropertyDefinitions();
		loadPropertyDefinitionTimes();
		loadPropertyValues();
		loadPropertyTimes();

		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	private void markAssigned(PersonPropertyId personPropertyId) {
		Integer nonDefaultPropertyIndex = nonDefaultBearingPropertyIds.get(personPropertyId);
		if (nonDefaultPropertyIndex != null) {
			nonDefaultChecks[nonDefaultPropertyIndex] = true;
		}
	}

	/**
	 * Returns true if and only if the person property id is valid.
	 */
	public boolean personPropertyIdExists(final PersonPropertyId personPropertyId) {
		return propertyDefinitions.containsKey(personPropertyId);
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		List<PersonId> people = peopleDataManager.getPeople();

		for (PersonPropertyId personPropertyId : propertyDefinitions.keySet()) {
			PropertyDefinition personPropertyDefinition = propertyDefinitions.get(personPropertyId);
			Double definitionTime = propertyDefinitionTimes.get(personPropertyId);
			boolean tracked = propertyTrackingPolicies.get(personPropertyId);
			builder.definePersonProperty(personPropertyId, personPropertyDefinition, definitionTime, tracked);
		}

		for (PersonPropertyId personPropertyId : propertyValues.keySet()) {
			IndexedPropertyManager indexedPropertyManager = propertyValues.get(personPropertyId);
			for (PersonId personId : people) {
				Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				builder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
			}
		}

		for (PersonPropertyId personPropertyId : propertyTimes.keySet()) {
			DoubleValueContainer doubleValueContainer = propertyTimes.get(personPropertyId);
			for (PersonId personId : people) {
				double propertyTime = doubleValueContainer.getValue(personId.getValue());
				builder.setPersonPropertyTime(personId, personPropertyId, propertyTime);
			}
		}

		dataManagerContext.releaseOutput(builder.build());
	}

	/**
	 * Updates the value of a person's property. Generates a corresponding
	 * {@linkplain PersonPropertyUpdateEvent}
	 *
	 *
	 * Throws {@link ContractException}
	 *
	 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
	 * <li>{@link PropertyError#NULL_PROPERTY_ID} if the person property id is
	 * null</li>
	 * <li>{@link PropertyError#UNKNOWN_PROPERTY_ID} if the person property id is
	 * unknown</li>
	 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_VALUE} if the property
	 * value is null</li>
	 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the property value is not
	 * compatible with the corresponding property definition</li>
	 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the corresponding property
	 * definition marks the property as immutable</li>
	 *
	 */
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId,
			final Object personPropertyValue) {
		dataManagerContext.releaseMutationEvent(
				new PersonPropertyUpdateMutationEvent(personId, personPropertyId, personPropertyValue));
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Precondition : the person property id is valid
	 */
	private void validatePersonPropertyAssignmentTimesTracked(final PersonPropertyId personPropertyId) {
		Boolean tracked = propertyTrackingPolicies.get(personPropertyId);
		if (!tracked) {
			throw new ContractException(PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validatePersonPropertyId(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!propertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
		}
	}

	private void validatePersonPropertyIdIsUnknown(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (propertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, personPropertyId);
		}
	}

	private void validatePersonPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validatePropertyDefinitionInitializationNotNull(
			PersonPropertyDefinitionInitialization propertyDefinitionInitialization) {
		if (propertyDefinitionInitialization == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION_INITIALIZATION);
		}
	}

	private void validateRegionId(RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionsDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	/*
	 * Preconditions: all arguments are non-null
	 */
	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
		}
	}

	private void verifyNonDefaultChecks() {

		boolean missingPropertyAssignments = false;

		for (boolean nonDefaultCheck : nonDefaultChecks) {
			if (!nonDefaultCheck) {
				missingPropertyAssignments = true;
				break;
			}
		}

		if (missingPropertyAssignments) {
			StringBuilder sb = new StringBuilder();
			int index = -1;
			boolean firstMember = true;
			for (PersonPropertyId personPropertyId : nonDefaultBearingPropertyIds.keySet()) {
				index++;
				if (!nonDefaultChecks[index]) {
					if (firstMember) {
						firstMember = false;
					} else {
						sb.append(", ");
					}
					sb.append(personPropertyId);
				}
			}
			throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, sb.toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonPropertiesDataManager [propertyDefinitions=");
		builder.append(propertyDefinitions);
		builder.append(", propertyDefinitionTimes=");
		builder.append(propertyDefinitionTimes);
		builder.append(", propertyValues=");
		builder.append(propertyValues);
		builder.append(", propertyTrackingPolicies=");
		builder.append(propertyTrackingPolicies);
		builder.append(", propertyTimes=");
		builder.append(propertyTimes);
		builder.append("]");
		return builder.toString();
	}

}
