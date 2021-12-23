package plugins.globals.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.Context;
import plugins.compartments.CompartmentPlugin;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyValueRecord;
import util.ContractException;

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

public final class GlobalDataManager {
	private final Context context;
	private Map<GlobalPropertyId, PropertyValueRecord> globalPropertyMap = new LinkedHashMap<>();
	private Set<GlobalComponentId> globalComponentIds = new LinkedHashSet<>();
	private Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

	/**
	 * Adds the global comoponent id.
	 * 
	 * @throws RuntimeException
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_COMPONENT_ID} if the
	 *             global component id is null</li>
	 *             <li>{@linkplain GlobalError#DUPLICATE_GLOBAL_COMPONENT_ID} if
	 *             the global component id was previously added</li>
	 */
	public void addGlobalComponentId(GlobalComponentId globalComponentId) {
		validateGlobalComponentIdForAddition(globalComponentId);
		globalComponentIds.add(globalComponentId);
	}

	/**
	 * Adds the global property definition.
	 * 
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_DEFINITION} if the
	 *             property definition is null</li>
	 *             <li>{@linkplain GlobalError#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
	 *             if the global property was previously added</li>
	 * 
	 * 
	 */
	public void addGlobalPropertyDefinition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {
		validateGlobalPropertyAddition(globalPropertyId, propertyDefinition);

		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(context);
		propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
		globalPropertyMap.put(globalPropertyId, propertyValueRecord);
		globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
	}

	private void validateGlobalPropertyAddition(GlobalPropertyId globalPropertyId, PropertyDefinition propertyDefinition) {

		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}
		
		if(propertyDefinition == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_DEFINITION);
		}

		if (globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION);
		}

	}

	private void validateGlobalComponentIdForAddition(GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_COMPONENT_ID);
		}
		if (globalComponentIds.contains(globalComponentId)) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID);
		}
	}

	/**
	 * Constructs the data manager
	 * 
	 */
	public GlobalDataManager(Context context) {
		this.context = context;
	}

	/**
	 * Returns the property definition for the given global property id. Returns
	 * null if the global property id is null or unknown.
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
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
	 * Gets the value of the global property.
	 * 
	 * @throws RuntimeException
	 *             <li>if the global property id is null</li>
	 *             <li>if the global property id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId) {

		return (T) globalPropertyMap.get(globalPropertyId).getValue();
	}

	/**
	 * Returns the time value for the last assignment to the given
	 * {@linkplain GlobalPropertyId}.
	 * 
	 * @throws RuntimeException
	 *             <li>if the global property id is null</li>
	 *             <li>if the global property id is unknown</li>
	 * 
	 */
	public double getGlobalPropertyTime(GlobalPropertyId globalPropertyId) {

		return globalPropertyMap.get(globalPropertyId).getAssignmentTime();
	}

	/**
	 * Set the value of the global property and updates the property assignment
	 * time.
	 * 
	 * @throws RuntimeException
	 *             <li>if the global property id is null</li>
	 *             <li>if the global property id is unknown</li>
	 */
	public void setGlobalPropertyValue(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {
		globalPropertyMap.get(globalPropertyId).setPropertyValue(globalPropertyValue);
	}

	/**
	 * Returns true if and only if the global property id exists. Returns false
	 * for null input.
	 */
	public boolean globalPropertyIdExists(final GlobalPropertyId globalPropertyId) {
		return globalPropertyMap.containsKey(globalPropertyId);
	}

	/**
	 * Returns the set of global component ids.
	 */
	@SuppressWarnings("unchecked")
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		Set<T> result = new LinkedHashSet<>();
		for (GlobalComponentId globalComponentId : globalComponentIds) {
			result.add((T) globalComponentId);
		}
		return result;
	}

}
