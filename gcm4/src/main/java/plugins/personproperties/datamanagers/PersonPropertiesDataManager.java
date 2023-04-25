package plugins.personproperties.datamanagers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.IdentifiableFunctionMap;
import nucleus.SimulationContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.events.PersonPropertyDefinitionEvent;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.util.properties.BooleanPropertyManager;
import plugins.util.properties.DoublePropertyManager;
import plugins.util.properties.EnumPropertyManager;
import plugins.util.properties.FloatPropertyManager;
import plugins.util.properties.IndexedPropertyManager;
import plugins.util.properties.IntPropertyManager;
import plugins.util.properties.ObjectPropertyManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
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
		PERSON_ID,
		CURRENT_VALUE,
		PREVIOUS_VALUE;//
	}

	private static record PersonPropertyDefinitionMutationEvent(PersonPropertyDefinitionInitialization propertyDefinitionInitialization) implements Event {
	}

	private static record PersonPropertyUpdateMutationEvent(PersonId personId, PersonPropertyId personPropertyId, Object personPropertyValue) implements Event {
	}

	private static void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private final Map<PersonPropertyId, PropertyDefinition> personPropertyDefinitions = new LinkedHashMap<>();

	private final Map<PersonPropertyId, IndexedPropertyManager> personPropertyManagerMap = new LinkedHashMap<>();

	private final Map<PersonPropertyId, DoubleValueContainer> personPropertyTimeMap = new LinkedHashMap<>();

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
			IdentifiableFunctionMap	.builder(PersonPropertyUpdateEvent.class)//
									.put(EventFunctionId.PERSON_PROPERTY_ID, e -> e.personPropertyId())//
									.put(EventFunctionId.REGION_ID, e -> regionsDataManager.getPersonRegion(e.personId()))//
									.put(EventFunctionId.PERSON_ID, e -> e.personId())//
									.put(EventFunctionId.CURRENT_VALUE, e -> e.currentPropertyValue()).put(EventFunctionId.PREVIOUS_VALUE, e -> e.previousPropertyValue()).build();//

	/**
	 * Constructs the person property data manager from the given plugin data
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_PLUGN_DATA}
	 *             if the plugin data is null</li>
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION_INITIALIZATION}
	 *             if the property definition initialization is null</li>
	 * 
	 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *             if the person property already exists</li>
	 * 
	 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if the property definition has no default value and there is
	 *             no included value assignment for some extant person</li>
	 */
	public void definePersonProperty(PersonPropertyDefinitionInitialization propertyDefinitionInitialization) {
		dataManagerContext.releaseMutationEvent(new PersonPropertyDefinitionMutationEvent(propertyDefinitionInitialization));
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for multiple population
	 * additions.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION} if
	 *             the count is negative</li>
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {
			for (final PersonPropertyId personPropertyId : personPropertyManagerMap.keySet()) {
				IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
				indexedPropertyManager.incrementCapacity(count);

				DoubleValueContainer doubleValueContainer = personPropertyTimeMap.get(personPropertyId);
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
		return EventFilter	.builder(PersonPropertyDefinitionEvent.class)//
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
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id
	 * and person id.
	 * 
	 * 
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is not known</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(PersonId personId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		validatePersonExists(personId);
		return EventFilter	.builder(PersonPropertyUpdateEvent.class)//
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is not known</li>
	 * 
	 * 
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return EventFilter	.builder(PersonPropertyUpdateEvent.class)//
							.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
							.build();

	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id
	 * and property value.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
	 *             person property value is null</li>
	 *
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(PersonPropertyId personPropertyId, Object propertyValue, boolean useCurrentValue) {
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyValueNotNull(propertyValue);
		EventFunctionId propertyValueEnum = useCurrentValue ? EventFunctionId.CURRENT_VALUE : EventFunctionId.PREVIOUS_VALUE;
		return EventFilter	.builder(PersonPropertyUpdateEvent.class).addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
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
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is not known</li>
	 * 
	 */
	public EventFilter<PersonPropertyUpdateEvent> getEventFilterForPersonPropertyUpdateEvent(RegionId regionId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		validateRegionId(regionId);
		return EventFilter	.builder(PersonPropertyUpdateEvent.class)//
							.addFunctionValuePair(functionMap.get(EventFunctionId.PERSON_PROPERTY_ID), personPropertyId)//
							.addFunctionValuePair(functionMap.get(EventFunctionId.REGION_ID), regionId)//
							.build();
	}

	private IndexedPropertyManager getIndexedPropertyManager(final SimulationContext simulationContext, final PropertyDefinition propertyDefinition, final int intialSize) {

		IndexedPropertyManager indexedPropertyManager;
		if (propertyDefinition.getType() == Boolean.class) {
			indexedPropertyManager = new BooleanPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Float.class) {
			indexedPropertyManager = new FloatPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Double.class) {
			indexedPropertyManager = new DoublePropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Byte.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Short.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Integer.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Long.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			indexedPropertyManager = new EnumPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else {
			indexedPropertyManager = new ObjectPropertyManager(simulationContext, propertyDefinition, intialSize);
		}
		return indexedPropertyManager;
	}

	/**
	 * Returns the list(no duplicates) people who have the given person property
	 * value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */
	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);

		final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */
		final int n = peopleDataManager.getPersonIdLimit();
		int count = 0;
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
		final List<PersonId> result = new ArrayList<>(count);

		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (peopleDataManager.personIndexExists(personIndex)) {
				final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
				final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				if (propertyValue.equals(personPropertyValue)) {
					result.add(personId);
				}
			}
		}

		return result;

	}

	/**
	 * Returns the number of people who have the given person property value.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_VALUE}
	 *             if the person property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             person property value is not compatible with the property
	 *             definition associated with the given person property id</li>
	 */
	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {

		validatePersonPropertyId(personPropertyId);
		final PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);

		/*
		 * We are not maintaining a map from property values to people. We first
		 * determine the number of people who will be returned so that we can
		 * size the resulting ArrayList properly.
		 */

		final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
		final int n = peopleDataManager.getPersonIdLimit();
		int count = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			if (peopleDataManager.personIndexExists(personIndex)) {
				final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
				final Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				if (propertyValue.equals(personPropertyValue)) {
					count++;
				}
			}
		}
		return count;

	}

	/**
	 * Returns the property definition for the given person property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 */
	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		validatePersonPropertyId(personPropertyId);
		return personPropertyDefinitions.get(personPropertyId);
	}

	/**
	 * Returns the person property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {

		final Set<T> result = new LinkedHashSet<>(personPropertyDefinitions.keySet().size());
		for (final PersonPropertyId personPropertyId : personPropertyDefinitions.keySet()) {
			result.add((T) personPropertyId);
		}

		return result;
	}

	/**
	 * Returns the time when the person's property was last assigned or zero if
	 * the value has never been assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 *             <li>{@linkplain PersonPropertyError#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if the person property does not have time tracking turned on
	 *             in the associated property definition</li>
	 * 
	 */
	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyAssignmentTimesTracked(personPropertyId);
		return personPropertyTimeMap.get(personPropertyId).getValue(personId.getValue());
	}

	/**
	 * Returns the current value of the person's property
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the person
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             person property id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		return (T) personPropertyManagerMap.get(personPropertyId).getPropertyValue(personId.getValue());
	}

	private void handlePersonImminentAdditionEvent(final DataManagerContext dataManagerContext, final PersonImminentAdditionEvent personImminentAdditionEvent) {

		PersonConstructionData personConstructionData = personImminentAdditionEvent.personConstructionData();

		PersonId personId = personImminentAdditionEvent.personId();

		List<PersonPropertyInitialization> personPropertyAssignments = personConstructionData.getValues(PersonPropertyInitialization.class);

		for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
			PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
			final Object personPropertyValue = personPropertyAssignment.getValue();
			validatePersonPropertyId(personPropertyId);
			validatePersonPropertyValueNotNull(personPropertyValue);
			final PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
			validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		}

		if (!nonDefaultBearingPropertyIds.isEmpty()) {
			clearNonDefaultChecks();
			for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
				PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
				markAssigned(personPropertyId);
			}
			verifyNonDefaultChecks();
		}

		for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
			PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
			final Object personPropertyValue = personPropertyAssignment.getValue();
			int pId = personId.getValue();
			IndexedPropertyManager propertyManager = personPropertyManagerMap.get(personPropertyId);
			propertyManager.setPropertyValue(pId, personPropertyValue);
			DoubleValueContainer doubleValueContainer = personPropertyTimeMap.get(personPropertyId);
			if (doubleValueContainer != null) {
				doubleValueContainer.setValue(pId, dataManagerContext.getTime());
			}
		}

	}

	private void handlePersonImminentRemovalEvent(final DataManagerContext dataManagerContext, final PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.personId();

		for (final PersonPropertyId personPropertyId : personPropertyManagerMap.keySet()) {
			final IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
			indexedPropertyManager.removeId(personId.getValue());
		}

	}

	private void handlePersonPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext, PersonPropertyDefinitionMutationEvent personPropertyDefinitionMutationEvent) {
		PersonPropertyDefinitionInitialization propertyDefinitionInitialization = personPropertyDefinitionMutationEvent.propertyDefinitionInitialization();
		validatePropertyDefinitionInitializationNotNull(propertyDefinitionInitialization);
		PersonPropertyId personPropertyId = propertyDefinitionInitialization.getPersonPropertyId();
		PropertyDefinition propertyDefinition = propertyDefinitionInitialization.getPropertyDefinition();

		validatePersonPropertyIdIsUnknown(personPropertyId);
		boolean checkAllPeopleHaveValues = propertyDefinition.getDefaultValue().isEmpty();

		for (Pair<PersonId, Object> pair : propertyDefinitionInitialization.getPropertyValues()) {
			PersonId personId = pair.getFirst();
			validatePersonExists(personId);
		}

		if (checkAllPeopleHaveValues) {
			addNonDefaultProperty(personPropertyId);
			int idLimit = peopleDataManager.getPersonIdLimit();
			BitSet coverageSet = new BitSet(idLimit);

			for (Pair<PersonId, Object> pair : propertyDefinitionInitialization.getPropertyValues()) {
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

		personPropertyDefinitions.put(personPropertyId, propertyDefinition);
		final IndexedPropertyManager propertyManager = getIndexedPropertyManager(dataManagerContext, propertyDefinition, 0);
		personPropertyManagerMap.put(personPropertyId, propertyManager);

		if (propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {
			DoubleValueContainer doubleValueContainer = new DoubleValueContainer(dataManagerContext.getTime());
			personPropertyTimeMap.put(personPropertyId, doubleValueContainer);
		}

		for (Pair<PersonId, Object> pair : propertyDefinitionInitialization.getPropertyValues()) {
			PersonId personId = pair.getFirst();
			int pId = personId.getValue();
			/*
			 * we do not have to validate the value since it is guaranteed to be
			 * consistent with the property definition by contract.
			 */
			Object value = pair.getSecond();
			propertyManager.setPropertyValue(pId, value);
		}

		if (dataManagerContext.subscribersExist(PersonPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonPropertyDefinitionEvent(personPropertyId));
		}

	}

	private void handlePersonPropertyUpdateMutationEvent(DataManagerContext dataManagerContext, PersonPropertyUpdateMutationEvent personPropertyUpdateMutationEvent) {
		PersonId personId = personPropertyUpdateMutationEvent.personId();
		PersonPropertyId personPropertyId = personPropertyUpdateMutationEvent.personPropertyId();
		Object personPropertyValue = personPropertyUpdateMutationEvent.personPropertyValue();
		validatePersonExists(personId);
		validatePersonPropertyId(personPropertyId);
		validatePersonPropertyValueNotNull(personPropertyValue);
		final PropertyDefinition propertyDefinition = personPropertyDefinitions.get(personPropertyId);
		validateValueCompatibility(personPropertyId, propertyDefinition, personPropertyValue);
		validatePropertyMutability(propertyDefinition);

		int pId = personId.getValue();
		IndexedPropertyManager propertyManager = personPropertyManagerMap.get(personPropertyId);
		Object oldValue = propertyManager.getPropertyValue(pId);
		propertyManager.setPropertyValue(pId, personPropertyValue);

		DoubleValueContainer doubleValueContainer = personPropertyTimeMap.get(personPropertyId);
		if (doubleValueContainer != null) {
			doubleValueContainer.setValue(pId, dataManagerContext.getTime());
		}

		if (dataManagerContext.subscribersExist(PersonPropertyUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonPropertyUpdateEvent(personId, personPropertyId, oldValue, personPropertyValue));
		}

	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		dataManagerContext.subscribe(PersonPropertyDefinitionMutationEvent.class, this::handlePersonPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(PersonPropertyUpdateMutationEvent.class, this::handlePersonPropertyUpdateMutationEvent);

		Set<PersonPropertyId> personPropertyIds = personPropertiesPluginData.getPersonPropertyIds();

		for (final PersonPropertyId personPropertyId : personPropertyIds) {
			PropertyDefinition personPropertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
			if (personPropertyDefinition.getDefaultValue().isEmpty()) {
				nonDefaultBearingPropertyIds.put(personPropertyId, nonDefaultBearingPropertyIds.size());
			}
			personPropertyDefinitions.put(personPropertyId, personPropertyDefinition);
			final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(dataManagerContext, personPropertyDefinition, 0);
			personPropertyManagerMap.put(personPropertyId, indexedPropertyManager);

			if (personPropertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME) {
				personPropertyTimeMap.put(personPropertyId, new DoubleValueContainer(0));
			}

		}

		nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];

		int personCount = FastMath.max(personPropertiesPluginData.getPersonCount(), peopleDataManager.getPersonIdLimit());

		if (nonDefaultBearingPropertyIds.isEmpty()) {
			for (int personIndex = 0; personIndex < personCount; personIndex++) {

				List<PersonPropertyInitialization> propertyValues = personPropertiesPluginData.getPropertyValues(personIndex);

				if (!propertyValues.isEmpty() && !peopleDataManager.personIndexExists(personIndex)) {
					throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
				}

				for (PersonPropertyInitialization personPropertyInitialization : propertyValues) {
					Object personPropertyValue = personPropertyInitialization.getValue();
					PersonPropertyId personPropertyId = personPropertyInitialization.getPersonPropertyId();
					IndexedPropertyManager propertyManager = personPropertyManagerMap.get(personPropertyId);
					propertyManager.setPropertyValue(personIndex, personPropertyValue);
				}

			}

		} else {
			for (int personIndex = 0; personIndex < personCount; personIndex++) {

				List<PersonPropertyInitialization> propertyValues = personPropertiesPluginData.getPropertyValues(personIndex);

				if (!propertyValues.isEmpty() && !peopleDataManager.personIndexExists(personIndex)) {
					throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
				}

				if (peopleDataManager.personIndexExists(personIndex)) {
					clearNonDefaultChecks();
					for (PersonPropertyInitialization personPropertyInitialization : propertyValues) {
						Object personPropertyValue = personPropertyInitialization.getValue();
						PersonPropertyId personPropertyId = personPropertyInitialization.getPersonPropertyId();
						markAssigned(personPropertyId);
						IndexedPropertyManager propertyManager = personPropertyManagerMap.get(personPropertyId);
						propertyManager.setPropertyValue(personIndex, personPropertyValue);
					}
					verifyNonDefaultChecks();
				}

			}

		}
		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonImminentAdditionEvent);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonImminentRemovalEvent);

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
		return personPropertyDefinitions.containsKey(personPropertyId);
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		List<PersonId> people = peopleDataManager.getPeople();

		for (PersonPropertyId personPropertyId : personPropertyDefinitions.keySet()) {
			PropertyDefinition personPropertyDefinition = personPropertyDefinitions.get(personPropertyId);
			builder.definePersonProperty(personPropertyId, personPropertyDefinition);
			IndexedPropertyManager indexedPropertyManager = personPropertyManagerMap.get(personPropertyId);
			for (PersonId personId : people) {
				Object propertyValue = indexedPropertyManager.getPropertyValue(personId.getValue());
				builder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
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
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@link PropertyError#NULL_PROPERTY_ID} if the person property id is
	 * null</li>
	 * <li>{@link PropertyError#UNKNOWN_PROPERTY_ID} if the person property id
	 * is unknown</li>
	 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_VALUE} if the
	 * property value is null</li>
	 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the property value is not
	 * compatible with the corresponding property definition</li>
	 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the corresponding property
	 * definition marks the property as immutable</li>
	 *
	 */
	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		dataManagerContext.releaseMutationEvent(new PersonPropertyUpdateMutationEvent(personId, personPropertyId, personPropertyValue));
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
		final PropertyDefinition personPropertyDefinition = personPropertyDefinitions.get(personPropertyId);
		if (personPropertyDefinition.getTimeTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throw new ContractException(PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED);
		}
	}

	private void validatePersonPropertyId(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!personPropertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, personPropertyId);
		}
	}

	private void validatePersonPropertyIdIsUnknown(final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (personPropertyDefinitions.containsKey(personPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, personPropertyId);
		}
	}

	private void validatePersonPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validatePropertyDefinitionInitializationNotNull(PersonPropertyDefinitionInitialization propertyDefinitionInitialization) {
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
	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void verifyNonDefaultChecks() {

		boolean missingPropertyAssignments = false;

		for (int i = 0; i < nonDefaultChecks.length; i++) {
			if (!nonDefaultChecks[i]) {
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

}
