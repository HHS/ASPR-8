package plugins.globalproperties.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.IdentifiableFunctionMap;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.GlobalPropertyInitialization;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A mutable data manager for global properties.
 * 
 *
 */

public final class GlobalPropertiesDataManager extends DataManager {

	/**
	 * A utility class for holding the value and assignment time for a property. On
	 * value assignment, this PropertyValueRecord records the current simulation
	 * time.
	 */
	public class PropertyValueRecord {

		private Object propertyValue;
		private double assignmentTime;
		private final DataManagerContext dataManagerContext;

		public PropertyValueRecord(DataManagerContext dataManagerContext) {
			this.dataManagerContext = dataManagerContext;
		}

		/**
		 * Returns the last assigned value
		 */
		public Object getValue() {		
			return propertyValue;
		}

		/**
		 * Sets the current value and records the assignment time to the current time
		 */
		public void setPropertyValue(Object propertyValue) {
			this.propertyValue = propertyValue;
			assignmentTime = dataManagerContext.getTime();
		}
		
		/**
		 * Sets the current value and assignment time 
		 */
		public void setPropertyValue(Object propertyValue, double assignmentTime) {
			this.propertyValue = propertyValue;
			this.assignmentTime = assignmentTime;
		}

		/**
		 * Returns the time of the last assignment
		 */
		public double getAssignmentTime() {
			return assignmentTime;
		}
	}
	
	private static enum EventFunctionId {
		GLOBAL_PROPERTY_ID; //

	}

	private static record GlobalPropertyUpdateMutationEvent(GlobalPropertyId globalPropertyId, Object globalPropertyValue) implements Event {
	}

	private static record GlobalPropertyInitializationMutationEvent(GlobalPropertyInitialization globalPropertyInitialization) implements Event {
	}

	private DataManagerContext dataManagerContext;

	private Map<GlobalPropertyId, PropertyValueRecord> globalPropertyMap = new LinkedHashMap<>();

	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

	private Map<GlobalPropertyId, Double> globalPropertyDefinitionTimes = new LinkedHashMap<>();

	private final GlobalPropertiesPluginData globalPropertiesPluginData;

	private IdentifiableFunctionMap<GlobalPropertyUpdateEvent> functionMap = //
			IdentifiableFunctionMap	.builder(GlobalPropertyUpdateEvent.class)//
									.put(EventFunctionId.GLOBAL_PROPERTY_ID, e -> e.globalPropertyId())//
									.build();//

	/**
	 * Constructs the data manager
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
	 *             if the global plugin data is null</li>
	 */
	public GlobalPropertiesDataManager(GlobalPropertiesPluginData globalPropertiesPluginData) {
		if (globalPropertiesPluginData == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA);
		}
		this.globalPropertiesPluginData = globalPropertiesPluginData;

	}

	/**
	 * 
	 * Returns an event that defines a new global property
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_INITIALIZATION}
	 *             if the global property initialization is null</li>
	 * 
	 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *             if the global property already exists</li>
	 * 
	 * 
	 */
	public void defineGlobalProperty(GlobalPropertyInitialization globalPropertyInitialization) {
		dataManagerContext.releaseMutationEvent(new GlobalPropertyInitializationMutationEvent(globalPropertyInitialization));
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyDefinitionEvent} events. Matches all such events.
	 *
	 */
	public EventFilter<GlobalPropertyDefinitionEvent> getEventFilterForGlobalPropertyDefinitionEvent() {
		return EventFilter	.builder(GlobalPropertyDefinitionEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyUpdateEvent} events. Matches all such events.
	 *
	 */
	public EventFilter<GlobalPropertyUpdateEvent> getEventFilterForGlobalPropertyUpdateEvent() {
		return EventFilter	.builder(GlobalPropertyUpdateEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyUpdateEvent} events. Matches on global property id.
	 *
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is not known</li>
	 * 
	 */
	public EventFilter<GlobalPropertyUpdateEvent> getEventFilterForGlobalPropertyUpdateEvent(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return EventFilter	.builder(GlobalPropertyUpdateEvent.class)//
							.addFunctionValuePair(functionMap.get(EventFunctionId.GLOBAL_PROPERTY_ID), globalPropertyId)//
							.build();
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 * 
	 */

	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyDefinitions.get(globalPropertyId);
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 * 
	 */

	public double getGlobalPropertyDefinitionTime(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyDefinitionTimes.get(globalPropertyId);
	}

	/**
	 * Returns the set of global property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		Set<T> result = new LinkedHashSet<>(globalPropertyDefinitions.keySet().size());
		for (GlobalPropertyId globalPropertyId : globalPropertyDefinitions.keySet()) {
			result.add((T) globalPropertyId);
		}
		return result;
	}

	/**
	 * Returns the time when the of the global property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 */

	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyMap.get(globalPropertyId).getAssignmentTime();
	}

	/**
	 * Returns the value of the global property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the global
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             global property id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return (T) globalPropertyMap.get(globalPropertyId).getValue();
	};

	/**
	 * Returns true if and only if the global property id exists. Returns false
	 * for null input.
	 */
	public boolean globalPropertyIdExists(final GlobalPropertyId globalPropertyId) {
		return globalPropertyMap.containsKey(globalPropertyId);
	}

	private void handleGlobalPropertyInitializationMutationEvent(DataManagerContext dataManagerContext, GlobalPropertyInitializationMutationEvent globalPropertyInitializationMutationEvent) {
		GlobalPropertyInitialization globalPropertyInitialization = globalPropertyInitializationMutationEvent.globalPropertyInitialization;
		validateGlobalPropertyInitializationNotNull(globalPropertyInitialization);
		GlobalPropertyId globalPropertyId = globalPropertyInitialization.getGlobalPropertyId();
		PropertyDefinition propertyDefinition = globalPropertyInitialization.getPropertyDefinition();
		validateGlobalPropertyIdIsUnknown(globalPropertyId);

		Object globalPropertyValue;
		Optional<Object> optional = globalPropertyInitialization.getValue();
		if (optional.isEmpty()) {
			globalPropertyValue = propertyDefinition.getDefaultValue().get();
		} else {
			globalPropertyValue = optional.get();
		}

		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
		propertyValueRecord.setPropertyValue(globalPropertyValue);
		globalPropertyMap.put(globalPropertyId, propertyValueRecord);
		globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		globalPropertyDefinitionTimes.put(globalPropertyId, dataManagerContext.getTime());

		if (dataManagerContext.subscribersExist(GlobalPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GlobalPropertyDefinitionEvent(globalPropertyId, globalPropertyValue));
		}

	}

	private void handleGlobalPropertyUpdateMutationEvent(DataManagerContext dataManagerContext, GlobalPropertyUpdateMutationEvent globalPropertyUpdateMutationEvent) {
		GlobalPropertyId globalPropertyId = globalPropertyUpdateMutationEvent.globalPropertyId();
		Object globalPropertyValue = globalPropertyUpdateMutationEvent.globalPropertyValue();
		validateGlobalPropertyId(globalPropertyId);
		validateGlobalPropertyValueNotNull(globalPropertyValue);
		final PropertyDefinition propertyDefinition = getGlobalPropertyDefinition(globalPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateValueCompatibility(globalPropertyId, propertyDefinition, globalPropertyValue);
		final Object oldPropertyValue = getGlobalPropertyValue(globalPropertyId);
		globalPropertyMap.get(globalPropertyId).setPropertyValue(globalPropertyValue);
		if (dataManagerContext.subscribersExist(GlobalPropertyUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GlobalPropertyUpdateEvent(globalPropertyId, oldPropertyValue, globalPropertyValue));
		}
	}

	/**
	 * Initializes the global properties data manager
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_DEF_TIME} if the
	 *             Global Properties Plugin Data contains a property definition
	 *             with a creation time that exceeds the current simulation
	 *             time</li>
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		
		this.dataManagerContext = dataManagerContext;
		validateGlobalPropertiesPluginData();

		dataManagerContext.subscribe(GlobalPropertyInitializationMutationEvent.class, this::handleGlobalPropertyInitializationMutationEvent);
		dataManagerContext.subscribe(GlobalPropertyUpdateMutationEvent.class, this::handleGlobalPropertyUpdateMutationEvent);

		for (GlobalPropertyId globalPropertyId : globalPropertiesPluginData.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = globalPropertiesPluginData.getGlobalPropertyDefinition(globalPropertyId);
			Double globalPropertyDefinitionTime = globalPropertiesPluginData.getGlobalPropertyDefinitionTime(globalPropertyId);
			globalPropertyDefinitionTimes.put(globalPropertyId, globalPropertyDefinitionTime);
			validateGlobalPropertyAddition(globalPropertyId, globalPropertyDefinition);
			Object globalPropertyValue = globalPropertiesPluginData.getGlobalPropertyValue(globalPropertyId);
			Double propertyTime = globalPropertiesPluginData.getGlobalPropertyTime(globalPropertyId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(globalPropertyValue, propertyTime);
			globalPropertyMap.put(globalPropertyId, propertyValueRecord);
			globalPropertyDefinitions.put(globalPropertyId, globalPropertyDefinition);
		}

		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}

	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();
		for (GlobalPropertyId globalPropertyId : globalPropertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = globalPropertyDefinitions.get(globalPropertyId);
			Double propertyDefinitionCreationTime = globalPropertyDefinitionTimes.get(globalPropertyId);
			builder.defineGlobalProperty(globalPropertyId, propertyDefinition, propertyDefinitionCreationTime);
			PropertyValueRecord propertyValueRecord = globalPropertyMap.get(globalPropertyId);
			Object value = propertyValueRecord.getValue();
			double assignmentTime = propertyValueRecord.getAssignmentTime();
			builder.setGlobalPropertyValue(globalPropertyId, value, assignmentTime);
		}
		dataManagerContext.releaseOutput(builder.build());
	}

	/**
	 * Set the value of the global property and updates the property assignment
	 * time.
	 *
	 * @throw {@link ContractException}
	 *        <li>{@link PropertyError.NULL_PROPERTY_ID} if the global property
	 *        id is null
	 *        <li>{@link PropertyError.UNKNOWN_PROPERTY_ID} if the global
	 *        property id is unknown
	 *        <li>{@link PropertyError.NULL_PROPERTY_VALUE} if the property
	 *        value is null
	 *        <li>{@link PropertyError.IMMUTABLE_VALUE} if the global property
	 *        definition indicates the property is not mutable
	 *        <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the property value
	 *        is incompatible with the property definition </blockquote></li>
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {

		dataManagerContext.releaseMutationEvent(new GlobalPropertyUpdateMutationEvent(globalPropertyId, globalPropertyValue));

	}

	private void validateGlobalPropertyAddition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {

		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION);
		}

	}

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!globalPropertyMap.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
		}
	}

	private void validateGlobalPropertyIdIsUnknown(final GlobalPropertyId globalPropertyId) {

		if (globalPropertyMap.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private void validateGlobalPropertyInitializationNotNull(GlobalPropertyInitialization globalPropertyInitialization) {
		if (globalPropertyInitialization == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_INITIALIZATION);
		}
	}

	private void validateGlobalPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateGlobalPropertiesPluginData() {
		double currentTime = dataManagerContext.getTime();
		for (GlobalPropertyId globalPropertyId : globalPropertiesPluginData.getGlobalPropertyIds()) {
			if (globalPropertiesPluginData.getGlobalPropertyDefinitionTime(globalPropertyId) > currentTime) {
				throw new ContractException(PropertyError.INCOMPATIBLE_DEF_TIME);
			}
		}
	}

}
