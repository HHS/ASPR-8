package plugins.util.properties;

import nucleus.SimulationContext;
import util.ContractException;
import util.arraycontainers.FloatValueContainer;

/**
 * Implementor of IndexedPropertyManager that compresses Float property values
 * into a float[]-based data structure.
 * 
 * @author Shawn Hatch
 *
 */
public final class FloatPropertyManager extends AbstractIndexedPropertyManager {
	/*
	 * A container, indexed by person id, that stores Double values as an array
	 * of float.
	 */
	private FloatValueContainer floatValueContainer;

	/**
	 * Constructs this FloatPropertyManager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *             if the property definition's type is not Boolean</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 */
	public FloatPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);
		if (propertyDefinition.getType() != Float.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,"Requires a property definition with float type");
		}
		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
		Float defaultValue = (Float) propertyDefinition.getDefaultValue().get();
		floatValueContainer = new FloatValueContainer(defaultValue, initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if(id<0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Float result = floatValueContainer.getValue(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		Float f = (Float) propertyValue;
		floatValueContainer.setValue(id, f);
	}

	@Override
	public void incrementCapacity(int count) {
		super.incrementCapacity(count);
		floatValueContainer.setCapacity(floatValueContainer.getCapacity() + count);
	}

}
