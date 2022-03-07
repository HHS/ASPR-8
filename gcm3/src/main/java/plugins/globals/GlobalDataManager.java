package plugins.globals;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.util.ContractException;
import plugins.globals.events.GlobalPropertyChangeObservationEvent;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;

/**
 * 
 * Provides event resolution for the {@linkplain GlobalPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain GlobalDataView}. Initializes
 * the data view from the {@linkplain GlobalInitialData} instance provided to
 * the plugin.
 * </P>
 * <P>
 * Creates dynamic global components.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain GlobalPropertyChangeObservationEvent}
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * 
 * 
 * <li>{@linkplain GlobalComponentConstructionEvent} <blockquote> Creates a
 * global component agent.  <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link GlobalError.NULL_GLOBAL_COMPONENT_ID} if the global component id
 * is null
 * <li>{@link GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID} if the global component
 * was previously asssigned to an agent
 * <li>{@link NucleusError.NULL_AGENT_CONTEXT_CONSUMER} if the agent context
 * consumers is null
 * 
 * </ul>
 * </blockquote></li>
 * 
 * 
 * 
 * <li>{@linkplain GlobalPropertyValueAssignmentEvent} <blockquote>Updates the
 * global property value and time in the {@linkplain GlobalDataView} and
 * generates a corresponding {@linkplain GlobalPropertyChangeObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_ID} if the global property id is
 * null
 * <li>{@link GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID} if the global property id
 * is unknown
 * <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_VALUE} if the property value is
 * null
 * <li>{@link PropertyError.IMMUTABLE_VALUE} if the global property definition
 * indicates the property is not mutable
 * <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the property value is
 * incompatible with the property definition </blockquote></li>
 * 
 *
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */

/**
 * Mutable data manager that backs the {@linkplain GlobalDataView}. This data
 * manager is for internal use by the {@link CompartmentPlugin} and should not
 * be published.
 * 
 * Manages global components and global properties.Limited validation of inputs
 * are performed and mutation methods have invocation ordering requirements.
 * 
 * @author Shawn Hatch
 *
 */

public final class GlobalDataManager extends DataManager {

	private DataManagerContext dataManagerContext;
	private Map<GlobalPropertyId, PropertyValueRecord> globalPropertyMap = new LinkedHashMap<>();
	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

	/**
	 * Adds the global property definition.
	 * 
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_DEFINITION}
	 *             if the property definition is null</li>
	 *             <li>{@linkplain GlobalError#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
	 *             if the global property was previously added</li>
	 * 
	 * 
	 */
	private void addGlobalPropertyDefinition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {
		validateGlobalPropertyAddition(globalPropertyId, propertyDefinition);

		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
		propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
		globalPropertyMap.put(globalPropertyId, propertyValueRecord);
		globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
	}

	private void validateGlobalPropertyAddition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {

		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (propertyDefinition == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_DEFINITION);
		}

		if (globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION);
		}

	}

	private final GlobalPluginData globalPluginData;

	/**
	 * Constructs the data manager
	 * 
	 * @throws ContractException
	 * <li>{@linkplain GlobalError#NULL_GLOBAL_PLUGIN_DATA} if the global plugin data is null</li>
	 */
	public GlobalDataManager(GlobalPluginData globalPluginData) {
		if(globalPluginData == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PLUGIN_DATA);
		}
		this.globalPluginData = globalPluginData;
		
	}

	/**
	 * Returns the property definition for the given {@link GlobalPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
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
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
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
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
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
	 *        <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_ID} if the global
	 *        property id is null
	 *        <li>{@link GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID} if the global
	 *        property id is unknown
	 *        <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_VALUE} if the property
	 *        value is null
	 *        <li>{@link PropertyError.IMMUTABLE_VALUE} if the global property
	 *        definition indicates the property is not mutable
	 *        <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the property value
	 *        is incompatible with the property definition </blockquote></li>
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {
		validateGlobalPropertyId(dataManagerContext, globalPropertyId);
		validateGlobalPropertyValueNotNull(dataManagerContext, globalPropertyValue);
		final PropertyDefinition propertyDefinition = getGlobalPropertyDefinition(globalPropertyId);
		validatePropertyMutability(dataManagerContext, propertyDefinition);
		validateValueCompatibility(dataManagerContext, globalPropertyId, propertyDefinition, globalPropertyValue);
		final Object oldPropertyValue = getGlobalPropertyValue(globalPropertyId);
		globalPropertyMap.get(globalPropertyId).setPropertyValue(globalPropertyValue);
		dataManagerContext.resolveEvent(new GlobalPropertyChangeObservationEvent(globalPropertyId, oldPropertyValue, globalPropertyValue));
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

		dataManagerContext.addEventLabeler(GlobalPropertyChangeObservationEvent.getEventLabeler());

		for (GlobalPropertyId globalPropertyId : globalPluginData.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = globalPluginData.getGlobalPropertyDefinition(globalPropertyId);
			addGlobalPropertyDefinition(globalPropertyId, globalPropertyDefinition);
			final Object globalPropertyValue = globalPluginData.getGlobalPropertyValue(globalPropertyId);
			if (globalPropertyValue != null) {
				final PropertyDefinition propertyDefinition = globalPluginData.getGlobalPropertyDefinition(globalPropertyId);
				validateValueCompatibility(dataManagerContext, globalPropertyId, propertyDefinition, globalPropertyValue);
				setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			}
		}

	}

	private void validateGlobalPropertyId(final DataManagerContext dataManagerContext, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalPropertyIdExists(globalPropertyId)) {
			throw new ContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

	private void validateGlobalPropertyValueNotNull(final DataManagerContext dataManagerContext, final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_VALUE);
		}
	}

	private void validatePropertyMutability(final DataManagerContext dataManagerContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateValueCompatibility(final DataManagerContext dataManagerContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateGlobalPropertyId(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalPropertyIdExists(globalPropertyId)) {
			throw new ContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

}
