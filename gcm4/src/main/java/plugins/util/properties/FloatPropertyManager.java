package plugins.util.properties;

import nucleus.SimulationContext;
import plugins.util.properties.arraycontainers.FloatValueContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Float property values
 * into a float[]-based data structure.
 * 
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
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li> 
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *             if the property definition's type is not Boolean</li>
	 */
	public FloatPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);
		if (propertyDefinition.getType() != Float.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,"Requires a property definition with float type");
		}
		Float defaultValue = 0F;
		if (propertyDefinition.getDefaultValue().isPresent()) {			
			defaultValue = (Float) propertyDefinition.getDefaultValue().get();
		}		
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
