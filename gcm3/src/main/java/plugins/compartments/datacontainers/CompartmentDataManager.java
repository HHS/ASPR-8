package plugins.compartments.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyValueRecord;

/**
 * Mutable data manager that backs the {@linkplain CompartmentDataView}. This
 * data manager is for internal use by the {@link CompartmentPlugin} and should
 * not be published.
 * 
 * All compartments and compartment properties are established during
 * construction and cannot be changed. Compartment property values are mutable.
 * Limited validation of inputs are performed and mutation methods have
 * invocation ordering requirements.
 * 
 * @author Shawn Hatch
 *
 */
public final class CompartmentDataManager {
	private final SimulationContext simulationContext;
	private Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> compartmentPropertyDefinitions = new LinkedHashMap<>();
	private Map<CompartmentId, Map<CompartmentPropertyId, PropertyValueRecord>> compartmentPropertyMap = new LinkedHashMap<>();

	/**
	 * Adds the compartment id. Preconditions: the compartment must be non-null
	 * and added exactly once.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment was previously added</li>
	 */
	public void addCompartmentId(CompartmentId compartmentId) {
		validateCompartmentIdForAddition(compartmentId);
		compartmentPropertyDefinitions.put(compartmentId, new LinkedHashMap<>());
		compartmentPropertyMap.put(compartmentId, new LinkedHashMap<>());
	}

	private void validateCompartmentIdForAddition(CompartmentId compartmentId) {
		if (compartmentId == null) {
			throw new RuntimeException("compartment id is null");
		}
		if (compartmentPropertyDefinitions.containsKey(compartmentId)) {
			throw new RuntimeException("compartment id previously added");
		}
	}

	/**
	 * Adds the compartment property definition. Preconditions: (1)the
	 * compartment must have already been added. (2)All inputs are non-null. (3)
	 * The compartment property was not previously added. (4) the property if
	 * valid definition contains a default value.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id was not previously added</li>
	 *             <li>if the compartment property id is null</li>
	 *             <li>if the compartment property was previously added</li>
	 *             <li>if the property definition does not contain a default
	 *             value</li>
	 * 
	 */
	public void addCompartmentPropertyDefinition(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, PropertyDefinition propertyDefinition) {
		validateCompartmentPropertyAddition(compartmentId, compartmentPropertyId, propertyDefinition);

		Map<CompartmentPropertyId, PropertyDefinition> propMap = compartmentPropertyDefinitions.get(compartmentId);
		propMap.put(compartmentPropertyId, propertyDefinition);
		Map<CompartmentPropertyId, PropertyValueRecord> valueMap = compartmentPropertyMap.get(compartmentId);
		PropertyValueRecord propertyValueRecord = new PropertyValueRecord(simulationContext);
		propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
		valueMap.put(compartmentPropertyId, propertyValueRecord);
	}

	private void validateCompartmentPropertyAddition(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, PropertyDefinition propertyDefinition) {
		if (compartmentId == null) {
			throw new RuntimeException("null compartment id");
		}
		if (!compartmentIdExists(compartmentId)) {
			throw new RuntimeException("unknown compartment -- add compartments before add compartment properties");
		}
		if (compartmentPropertyId == null) {
			throw new RuntimeException("null compartment property id");
		}
		if (compartmentPropertyIdExists(compartmentId, compartmentPropertyId)) {
			throw new RuntimeException("compartment property previously added");
		}

		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("property definition does not contain a default value");
		}
	}

	/**
	 * Sets the value of the compartment property. Preconditions: (1)All inputs
	 * are non-null (2)The compartment and compartment property definitions were
	 * previously added. No validation is performed.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id was not previously added</li>
	 *             <li>if the compartment property id is null</li>
	 *             <li>if the compartment property was previously added</li> 
	 */
	public void setCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, Object compartmentPropertyValue) {
		compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).setPropertyValue(compartmentPropertyValue);
	}

	/**
	 * Creates a Compartment Data Manager from the given resolver context.
	 * Preconditions: The context must be a valid and non-null.
	 */
	public CompartmentDataManager(SimulationContext simulationContext) {
		this.simulationContext = simulationContext;
	}

	/**
	 * Returns the {@link PropertyDefinition} associated with the given
	 * {@link CompartmentId} and {@link CompartmentPropertyId}. The compartment
	 * and property id must be valid.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id does not exist</li>
	 * 
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
	}

	/**
	 * Returns the compartment property ids for the given compartment. The
	 * compartment must be a valid compartment.
	 */
	@SuppressWarnings("unchecked")
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId) {
		Set<T> result = new LinkedHashSet<>();
		Map<CompartmentPropertyId, PropertyDefinition> map = compartmentPropertyDefinitions.get(compartmentId);
		if (map != null) {
			for (CompartmentPropertyId compartmentPropertyId : map.keySet()) {
				result.add((T) compartmentPropertyId);
			}
		}
		return result;
	}

	/**
	 * 
	 * Returns the value of the compartment property. Preconditions: (1)All
	 * inputs are non-null and valid. No validation is performed.
	 * 
	 * @throws RuntimeException
	 *             
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id was not previously added</li>
	 *             <li>if the compartment property id is null</li>
	 *             <li>if the compartment property was not previously added</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		return (T) compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).getValue();
	}

	/**
	 * Returns the time value for the last assignment to the given
	 * {@linkplain CompartmentId} and {@linkplain CompartmentPropertyId}. The
	 * {@link CompartmentId} and {@link CompartmentPropertyId} must be valid.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 *             <li>if the compartment property id is null</li>
	 *             <li>if the compartment property id is unknown</li>
	 * 
	 */
	public double getCompartmentPropertyTime(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		return compartmentPropertyMap.get(compartmentId).get(compartmentPropertyId).getAssignmentTime();
	}

	/**
	 * Returns the set of {@link CompartmentId} values that are defined by the
	 * {@link CompartmentInitialData}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends CompartmentId> Set<T> getCompartmentIds() {
		Set<T> result = new LinkedHashSet<>();
		for (CompartmentId compartmentId : compartmentPropertyMap.keySet()) {
			result.add((T) compartmentId);
		}
		return result;
	}

	/**
	 * Return true if and only if the given {@link CompartmentId} exits. Null
	 * tolerant.
	 */
	public boolean compartmentIdExists(CompartmentId compartmentId) {
		return compartmentPropertyMap.keySet().contains(compartmentId);
	}

	/**
	 * Returns true if and only if the given {@link CompartmentPropertyId} is
	 * associated with the given {@link CompartmentId}. Tolerates nulls.
	 */
	public boolean compartmentPropertyIdExists(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		Map<CompartmentPropertyId, PropertyValueRecord> map = compartmentPropertyMap.get(compartmentId);
		if ((map != null)) {
			return map.containsKey(compartmentPropertyId);
		}
		return false;
	}

}
