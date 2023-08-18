package gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.IdentifiableFunctionMap;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertiesError;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyInitialization;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A mutable data manager for global properties.
 */
public final class GlobalPropertiesDataManager extends DataManager {

	private static enum EventFunctionId {
		GLOBAL_PROPERTY_ID; //

	}

	private static record GlobalPropertyUpdateMutationEvent(GlobalPropertyId globalPropertyId,
			Object globalPropertyValue) implements Event {
	}

	private static record GlobalPropertyInitializationMutationEvent(
			GlobalPropertyInitialization globalPropertyInitialization) implements Event {
	}

	private DataManagerContext dataManagerContext;

	private Map<GlobalPropertyId, Object> globalPropertyValues;

	private Map<GlobalPropertyId, Double> globalPropertyTimes;

	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions;

	private Map<GlobalPropertyId, Double> globalPropertyDefinitionTimes;

	private final GlobalPropertiesPluginData globalPropertiesPluginData;

	private IdentifiableFunctionMap<GlobalPropertyUpdateEvent> functionMap = //
			IdentifiableFunctionMap.builder(GlobalPropertyUpdateEvent.class)//
					.put(EventFunctionId.GLOBAL_PROPERTY_ID, e -> e.globalPropertyId())//
					.build();//

	/**
	 * Constructs the data manager
	 * 
	 * @throws ContractException {@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
	 *                           if the global plugin data is null
	 */
	public GlobalPropertiesDataManager(GlobalPropertiesPluginData globalPropertiesPluginData) {
		if (globalPropertiesPluginData == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA);
		}
		this.globalPropertiesPluginData = globalPropertiesPluginData;

	}

	/**
	 * Returns an event that defines a new global property
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_INITIALIZATION}
	 *                           if the global property initialization is null</li>
	 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *                           if the global property already exists</li>
	 *                           </ul>
	 */
	public void defineGlobalProperty(GlobalPropertyInitialization globalPropertyInitialization) {
		dataManagerContext
				.releaseMutationEvent(new GlobalPropertyInitializationMutationEvent(globalPropertyInitialization));
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyDefinitionEvent} events. Matches all such events.
	 */
	public EventFilter<GlobalPropertyDefinitionEvent> getEventFilterForGlobalPropertyDefinitionEvent() {
		return EventFilter.builder(GlobalPropertyDefinitionEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyUpdateEvent} events. Matches all such events.
	 */
	public EventFilter<GlobalPropertyUpdateEvent> getEventFilterForGlobalPropertyUpdateEvent() {
		return EventFilter.builder(GlobalPropertyUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GlobalPropertyUpdateEvent} events. Matches on global property id.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is not known</li>
	 *                           </ul>
	 */
	public EventFilter<GlobalPropertyUpdateEvent> getEventFilterForGlobalPropertyUpdateEvent(
			GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return EventFilter.builder(GlobalPropertyUpdateEvent.class)//
				.addFunctionValuePair(functionMap.get(EventFunctionId.GLOBAL_PROPERTY_ID), globalPropertyId)//
				.build();
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is unknown</li>
	 *                           </ul>
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyDefinitions.get(globalPropertyId);
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is unknown</li>
	 *                           </ul>
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
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is unknown</li>
	 *                           </ul>
	 */
	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		Double result = globalPropertyTimes.get(globalPropertyId);
		if (result == null) {
			result = globalPropertyDefinitionTimes.get(globalPropertyId);
		}
		return result;
	}

	/**
	 * Returns the value of the global property.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the global property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the global property id is unknown</li>
	 *                           </ul>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		Object result = globalPropertyValues.get(globalPropertyId);
		if (result == null) {
			result = globalPropertyDefinitions.get(globalPropertyId).getDefaultValue().get();
		}
		return (T) result;
	};

	/**
	 * Returns true if and only if the global property id exists. Returns false for
	 * null input.
	 */
	public boolean globalPropertyIdExists(final GlobalPropertyId globalPropertyId) {
		return globalPropertyDefinitions.containsKey(globalPropertyId);
	}

	private void handleGlobalPropertyInitializationMutationEvent(DataManagerContext dataManagerContext,
			GlobalPropertyInitializationMutationEvent globalPropertyInitializationMutationEvent) {
		GlobalPropertyInitialization globalPropertyInitialization = globalPropertyInitializationMutationEvent.globalPropertyInitialization;
		validateGlobalPropertyInitializationNotNull(globalPropertyInitialization);
		GlobalPropertyId globalPropertyId = globalPropertyInitialization.getGlobalPropertyId();
		validateGlobalPropertyIdIsUnknown(globalPropertyId);

		PropertyDefinition propertyDefinition = globalPropertyInitialization.getPropertyDefinition();
		globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
		globalPropertyDefinitionTimes.put(globalPropertyId, dataManagerContext.getTime());

		Object globalPropertyValue;
		Optional<Object> optional = globalPropertyInitialization.getValue();
		if (optional.isEmpty()) {
			globalPropertyValue = propertyDefinition.getDefaultValue().get();
		} else {
			globalPropertyValue = optional.get();
			globalPropertyValues.put(globalPropertyId, globalPropertyValue);
			globalPropertyTimes.put(globalPropertyId, dataManagerContext.getTime());
		}

		if (dataManagerContext.subscribersExist(GlobalPropertyDefinitionEvent.class)) {
			dataManagerContext
					.releaseObservationEvent(new GlobalPropertyDefinitionEvent(globalPropertyId, globalPropertyValue));
		}

	}

	private void handleGlobalPropertyUpdateMutationEvent(DataManagerContext dataManagerContext,
			GlobalPropertyUpdateMutationEvent globalPropertyUpdateMutationEvent) {
		GlobalPropertyId globalPropertyId = globalPropertyUpdateMutationEvent.globalPropertyId();
		Object globalPropertyValue = globalPropertyUpdateMutationEvent.globalPropertyValue();
		validateGlobalPropertyId(globalPropertyId);
		validateGlobalPropertyValueNotNull(globalPropertyValue);
		final PropertyDefinition propertyDefinition = getGlobalPropertyDefinition(globalPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateValueCompatibility(globalPropertyId, propertyDefinition, globalPropertyValue);
		final Object oldPropertyValue = getGlobalPropertyValue(globalPropertyId);
		globalPropertyValues.put(globalPropertyId, globalPropertyValue);
		globalPropertyTimes.put(globalPropertyId, dataManagerContext.getTime());
		if (dataManagerContext.subscribersExist(GlobalPropertyUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(
					new GlobalPropertyUpdateEvent(globalPropertyId, oldPropertyValue, globalPropertyValue));
		}
	}

	/**
	 * Initializes the global properties data manager
	 * 
	 * @throws ContractException {@linkplain PropertyError#INCOMPATIBLE_DEF_TIME} if
	 *                           the Global Properties Plugin Data contains a
	 *                           property definition with a creation time that
	 *                           exceeds the current simulation time
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);

		this.dataManagerContext = dataManagerContext;
		validateGlobalPropertiesPluginData();

		dataManagerContext.subscribe(GlobalPropertyInitializationMutationEvent.class,
				this::handleGlobalPropertyInitializationMutationEvent);
		dataManagerContext.subscribe(GlobalPropertyUpdateMutationEvent.class,
				this::handleGlobalPropertyUpdateMutationEvent);

		globalPropertyValues = globalPropertiesPluginData.getGlobalPropertyValues();

		globalPropertyTimes = globalPropertiesPluginData.getGlobalPropertyTimes();

		globalPropertyDefinitions = globalPropertiesPluginData.getGlobalPropertyDefinitions();

		globalPropertyDefinitionTimes = globalPropertiesPluginData.getGlobalPropertyDefinitionTimes();

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
		}
		for (GlobalPropertyId globalPropertyId : globalPropertyValues.keySet()) {
			Object value = globalPropertyValues.get(globalPropertyId);
			double time = globalPropertyTimes.get(globalPropertyId);
			builder.setGlobalPropertyValue(globalPropertyId, value, time);
		}

		dataManagerContext.releaseOutput(builder.build());

	}

	/**
	 * Set the value of the global property and updates the property assignment
	 * time.
	 *
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link PropertyError.NULL_PROPERTY_ID} if the
	 *                           global property id is null</li>
	 *                           <li>{@link PropertyError.UNKNOWN_PROPERTY_ID} if
	 *                           the global property id is unknown</li>
	 *                           <li>{@link PropertyError.NULL_PROPERTY_VALUE} if
	 *                           the property value is null</li>
	 *                           <li>{@link PropertyError.IMMUTABLE_VALUE} if the
	 *                           global property definition indicates the property
	 *                           is not mutable</li>
	 *                           <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the
	 *                           property value is incompatible with the property
	 *                           definition </blockquote></li>
	 *                           </ul>
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {

		dataManagerContext
				.releaseMutationEvent(new GlobalPropertyUpdateMutationEvent(globalPropertyId, globalPropertyValue));

	}

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, globalPropertyId);
		}
	}

	private void validateGlobalPropertyIdIsUnknown(final GlobalPropertyId globalPropertyId) {

		if (globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private void validateGlobalPropertyInitializationNotNull(
			GlobalPropertyInitialization globalPropertyInitialization) {
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

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GlobalPropertiesDataManager [globalPropertyDefinitions=");
		builder.append(globalPropertyDefinitions);
		builder.append(", globalPropertyDefinitionTimes=");
		builder.append(globalPropertyDefinitionTimes);
		builder.append(", globalPropertyValues=");
		builder.append(globalPropertyValues);
		builder.append(", globalPropertyTimes=");
		builder.append(globalPropertyTimes);
		builder.append("]");
		return builder.toString();
	}

}
