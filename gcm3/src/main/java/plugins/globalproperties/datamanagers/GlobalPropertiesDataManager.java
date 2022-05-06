package plugins.globalproperties.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;
import util.errors.ContractException;

/**
 * A mutable data manager for global properties.
 * 
 * @author Shawn Hatch
 *
 */

public final class GlobalPropertiesDataManager extends DataManager {

	private DataManagerContext dataManagerContext;
	private Map<GlobalPropertyId, PropertyValueRecord> globalPropertyMap = new LinkedHashMap<>();
	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

	private void validateGlobalPropertyAddition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {

		if (globalPropertyId == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (propertyDefinition == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_DEFINITION);
		}

		if (globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalPropertiesError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION);
		}

	}

	private final GlobalPropertiesPluginData globalPropertiesPluginData;

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
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_ID}
	 *             if the global property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *             if the global property id is unknown</li>
	 * 
	 */

	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyDefinitions.get(globalPropertyId);
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
	 * Returns the value of the global property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_ID}
	 *             if the global property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *             if the global property id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return (T) globalPropertyMap.get(globalPropertyId).getValue();
	}

	/**
	 * Returns the time when the of the global property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_ID}
	 *             if the global property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#UNKNOWN_GLOBAL_PROPERTY_ID}
	 *             if the global property id is unknown</li>
	 */

	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {
		validateGlobalPropertyId(globalPropertyId);
		return globalPropertyMap.get(globalPropertyId).getAssignmentTime();
	}

	/**
	 * Set the value of the global property and updates the property assignment
	 * time.
	 *
	 * @throw {@link ContractException}
	 *        <li>{@link GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID} if the
	 *        global property id is null
	 *        <li>{@link GlobalPropertiesError.UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *        the global property id is unknown
	 *        <li>{@link GlobalPropertiesError.NULL_GLOBAL_PROPERTY_VALUE} if
	 *        the property value is null
	 *        <li>{@link PropertyError.IMMUTABLE_VALUE} if the global property
	 *        definition indicates the property is not mutable
	 *        <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the property value
	 *        is incompatible with the property definition </blockquote></li>
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {
		validateGlobalPropertyId(globalPropertyId);
		validateGlobalPropertyValueNotNull(globalPropertyValue);
		final PropertyDefinition propertyDefinition = getGlobalPropertyDefinition(globalPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateValueCompatibility(globalPropertyId, propertyDefinition, globalPropertyValue);
		final Object oldPropertyValue = getGlobalPropertyValue(globalPropertyId);
		globalPropertyMap.get(globalPropertyId).setPropertyValue(globalPropertyValue);
		dataManagerContext.releaseEvent(new GlobalPropertyUpdateEvent(globalPropertyId, oldPropertyValue, globalPropertyValue));
	}

	/**
	 * Returns true if and only if the global property id exists. Returns false
	 * for null input.
	 */
	public boolean globalPropertyIdExists(final GlobalPropertyId globalPropertyId) {
		return globalPropertyMap.containsKey(globalPropertyId);
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;

		dataManagerContext.addEventLabeler(GlobalPropertyUpdateEvent.getEventLabeler());

		for (GlobalPropertyId globalPropertyId : globalPropertiesPluginData.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = globalPropertiesPluginData.getGlobalPropertyDefinition(globalPropertyId);
			validateGlobalPropertyAddition(globalPropertyId, globalPropertyDefinition);
			Object globalPropertyValue = globalPropertiesPluginData.getGlobalPropertyValue(globalPropertyId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(globalPropertyValue);
			globalPropertyMap.put(globalPropertyId, propertyValueRecord);
			globalPropertyDefinitions.put(globalPropertyId, globalPropertyDefinition);
		}

	}

	/**
	 * 
	 * Defines a new global property
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_ID}
	 *             if the global property id is null</li>
	 *             <li>{@linkplain GlobalPropertiesError#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
	 *             if the global property already exists</li>
	 *             <li>{@linkplain PropertyError.NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition has no default value</li>
	 *	 
	 * 
	 */
	public void defineGlobalProperty(GlobalPropertyId globalPropertyId, PropertyDefinition globalPropertyDefinition) {

		validateGlobalPropertyIdIsUnknown(globalPropertyId);
		validatePropertyDefinitionNotNull(globalPropertyDefinition);
		validatePropertyDefinitionHasDefaultValue(globalPropertyDefinition);
		Object globalPropertyValue = globalPropertyDefinition.getDefaultValue().get();
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
		propertyValueRecord.setPropertyValue(globalPropertyValue);		
		globalPropertyMap.put(globalPropertyId, propertyValueRecord);
		globalPropertyDefinitions.put(globalPropertyId, globalPropertyDefinition);
		
		dataManagerContext.releaseEvent(new GlobalPropertyDefinitionEvent(globalPropertyId,globalPropertyValue));
	}

	private void validateGlobalPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_VALUE);
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

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalPropertyMap.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalPropertiesError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

	private void validateGlobalPropertyIdIsUnknown(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (globalPropertyMap.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalPropertiesError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private void validatePropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private void validatePropertyDefinitionHasDefaultValue(PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
	}

}
