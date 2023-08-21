package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.IdentifiableFunctionMap;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * Published data view that provides attribute information.
 */

public final class AttributesDataManager extends DataManager {

	private DataManagerContext dataManagerContext;

	private Map<AttributeId, AttributeDefinition> attributeDefinitions = new LinkedHashMap<>();

	private final Map<AttributeId, Map<PersonId, Object>> attributeValues = new LinkedHashMap<>();

	/**
	 * Returns the attribute definition associated with the given attribute id
	 * without validation.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
	 *                           if the attribute id is null</li>
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
	 *                           if the attribute id unknown</li>
	 *                           </ul>
	 */
	public AttributeDefinition getAttributeDefinition(final AttributeId attributeId) {
		validateAttributeId(attributeId);
		return attributeDefinitions.get(attributeId);
	}

	private void validateAttributeId(AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}

		if (!attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID);
		}

	}

	/**
	 * Returns the attribute ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends AttributeId> Set<T> getAttributeIds() {
		final Set<T> result = new LinkedHashSet<>(attributeDefinitions.keySet().size());
		for (final AttributeId attributeId : attributeDefinitions.keySet()) {
			result.add((T) attributeId);
		}
		return result;
	}

	/**
	 * Returns the attribute value associated with the given attribute id
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
	 *                           if the attribute id is null</li>
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
	 *                           if the attribute id unknown</li>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if
	 *                           the person id unknown</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttributeValue(final PersonId personId, final AttributeId attributeId) {
		validateAttributeId(attributeId);
		validatePersonId(personId);
		Object value = null;
		Map<PersonId, Object> map = attributeValues.get(attributeId);
		if (map != null) {
			value = attributeValues.get(attributeId).get(personId);
		}
		if (value == null) {
			value = attributeDefinitions.get(attributeId).getDefaultValue();
		}
		return (T) value;
	}

	private void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private PeopleDataManager peopleDataManager;

	/**
	 * Returns true if and only if the attribute is contained
	 */
	public boolean attributeExists(final AttributeId attributeId) {
		return attributeDefinitions.containsKey(attributeId);
	}

	private final AttributesPluginData attributesPluginData;

	/**
	 * Constructs this data manager from the given context
	 */
	public AttributesDataManager(AttributesPluginData attributesPluginData) {
		if (attributesPluginData == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA);
		}
		this.attributesPluginData = attributesPluginData;
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);

		attributeDefinitions = attributesPluginData.getAttributeDefinitions();

		Map<AttributeId, List<Object>> attributeValues2 = attributesPluginData.getAttributeValues();

		for (AttributeId attributeId : attributeValues2.keySet()) {
			Map<PersonId, Object> personAttributesMap = new LinkedHashMap<>();
			attributeValues.put(attributeId, personAttributesMap);
			List<Object> propertyValues = attributeValues2.get(attributeId);
			int n = FastMath.max(peopleDataManager.getPersonIdLimit(), propertyValues.size());
			for (int i = 0; i < n; i++) {
				if (peopleDataManager.personIndexExists(i)) {
					if (i < propertyValues.size()) {
						Object propertyValue = propertyValues.get(i);
						if (propertyValue != null) {
							PersonId personId = peopleDataManager.getBoxedPersonId(i).get();
							personAttributesMap.put(personId, propertyValue);
						}
					}
				} else {
					if (i < propertyValues.size()) {
						Object propertyValue = propertyValues.get(i);
						if (propertyValue != null) {
							throw new ContractException(AttributeError.UNKNOWN_PERSON_HAS_ATTRIBUTE_VALUE_ASSIGNMENT,
									"unknown person(" + i + ") has attribute value for " + attributeId);
						}
					}
				}
			}
		}

		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		dataManagerContext.subscribe(AttributeUpdateMutationEvent.class, this::handleAttributeUpdateMutationEvent);

		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}

	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {

		AttributesPluginData.Builder builder = AttributesPluginData.builder();

		for (AttributeId attributeId : attributeDefinitions.keySet()) {
			AttributeDefinition attributeDefinition = attributeDefinitions.get(attributeId);
			builder.defineAttribute(attributeId, attributeDefinition);
		}

		for (AttributeId attributeId : attributeValues.keySet()) {
			Map<PersonId, Object> map = attributeValues.get(attributeId);
			if (map != null) {
				for (PersonId personId : map.keySet()) {
					Object value = map.get(personId);
					builder.setPersonAttributeValue(personId, attributeId, value);
				}
			}
		}

		dataManagerContext.releaseOutput(builder.build());
	}

	private void handlePersonRemovalEvent(final DataManagerContext dataManagerContext,
			final PersonRemovalEvent personRemovalEvent) {
		for (AttributeId attributeId : attributeValues.keySet()) {
			Map<PersonId, Object> map = attributeValues.get(attributeId);
			if (map != null) {
				map.remove(personRemovalEvent.personId());
			}
		}
	}

	private static record AttributeUpdateMutationEvent(PersonId personId, AttributeId attributeId, Object value)
			implements Event {
	}

	/**
	 * Updates the person's current attribute value. Generates a corresponding
	 * {@linkplain AttributeUpdateEvent} Throws {@link ContractException}
	 * <ul>
	 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
	 * <li>{@link AttributeError#NULL_ATTRIBUTE_ID} if the attribute id is null</li>
	 * <li>{@link AttributeError#UNKNOWN_ATTRIBUTE_ID} if the attribute id is
	 * unknown</li>
	 * <li>{@link AttributeError#NULL_ATTRIBUTE_VALUE} if the attribute value is
	 * null</li>
	 * <li>{@link AttributeError#INCOMPATIBLE_VALUE} if the attribute value is
	 * incompatible with the associated attribute definition</li>
	 * </ul>
	 */
	public void setAttributeValue(final PersonId personId, final AttributeId attributeId, final Object value) {

		dataManagerContext.releaseMutationEvent(new AttributeUpdateMutationEvent(personId, attributeId, value));
	}

	private void handleAttributeUpdateMutationEvent(DataManagerContext dataManagerContext,
			AttributeUpdateMutationEvent attributeUpdateMutationEvent) {
		AttributeId attributeId = attributeUpdateMutationEvent.attributeId();
		PersonId personId = attributeUpdateMutationEvent.personId();
		Object value = attributeUpdateMutationEvent.value();

		validatePersonExists(dataManagerContext, personId);
		validateAttributeId(dataManagerContext, attributeId);
		validateValueNotNull(dataManagerContext, value);
		final AttributeDefinition attributeDefinition = getAttributeDefinition(attributeId);
		validateValueCompatibility(dataManagerContext, attributeId, attributeDefinition, value);
		Object previousValue = getAttributeValue(personId, attributeId);
		Map<PersonId, Object> map = attributeValues.get(attributeId);
		if (map == null) {
			map = new LinkedHashMap<>();
			attributeValues.put(attributeId, map);
		}
		map.put(personId, value);
		if (dataManagerContext.subscribersExist(AttributeUpdateEvent.class)) {
			dataManagerContext
					.releaseObservationEvent(new AttributeUpdateEvent(personId, attributeId, previousValue, value));
		}

	}

	private void validatePersonExists(final DataManagerContext dataManagerContext, final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validateValueNotNull(final DataManagerContext dataManagerContext, final Object value) {
		if (value == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	private static void validateValueCompatibility(final DataManagerContext dataManagerContext,
			final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Object value) {
		if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
			throw new ContractException(AttributeError.INCOMPATIBLE_VALUE,
					"Attribute value " + value + " is not of type " + attributeDefinition.getType().getName()
							+ " and does not match definition of " + attributeId);
		}
	}

	private void validateAttributeId(DataManagerContext dataManagerContext, final AttributeId attributeId) {
		if (attributeId == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (!attributeExists(attributeId)) {
			throw new ContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
	}

	private static enum EventFunctionId {
		ATTRIBUTE_ID, //
	}

	private IdentifiableFunctionMap<AttributeUpdateEvent> functionMap = //
			IdentifiableFunctionMap.builder(AttributeUpdateEvent.class)//
					.put(EventFunctionId.ATTRIBUTE_ID, e -> e.attributeId())//
					.build();//

	/**
	 * Returns an event filter used to subscribe to {@link AttributeUpdateEvent}
	 * events. Matches on attribute id.
	 * 
	 * @throws ContractException <ul>
	 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_ID}
	 *                           if the attribute id is null</li>
	 *                           <li>{@linkplain AttributeError#UNKNOWN_ATTRIBUTE_ID}
	 *                           if the attribute id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<AttributeUpdateEvent> getEventFilterForAttributeUpdateEvent(AttributeId attributeId) {
		validateAttributeId(attributeId);
		return EventFilter.builder(AttributeUpdateEvent.class)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.ATTRIBUTE_ID), attributeId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link AttributeUpdateEvent}
	 * events. Matches all such events.
	 */
	public EventFilter<AttributeUpdateEvent> getEventFilterForAttributeUpdateEvent() {

		return EventFilter.builder(AttributeUpdateEvent.class)//
				.build();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AttributesDataManager [attributeDefinitions=");
		builder.append(attributeDefinitions);
		builder.append(", attributeValues=");
		builder.append(attributeValues);
		builder.append("]");
		return builder.toString();
	}

}
