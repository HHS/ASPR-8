package plugins.util.properties;

import nucleus.SimulationContext;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Double property values
 * into a double[]-based data structure.
 * 
 *
 */
public final class DoublePropertyManager extends AbstractIndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores Double values as an array
	 * of double.
	 */
	private DoubleValueContainer doubleValueContainer;

	/**
	 * Constructs this DoublePropertyManager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *             if the property definition's type is not Double</li>
	 *            
	 * 
	 */
	public DoublePropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);

		if (propertyDefinition.getType() != Double.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "Requires a property definition with Double type ");
		}
		Double defaultValue = 0D;
		if (propertyDefinition.getDefaultValue().isPresent()) {			
			defaultValue = (Double) propertyDefinition.getDefaultValue().get();
		}
		doubleValueContainer = new DoubleValueContainer(defaultValue, initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if(id<0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Double result = doubleValueContainer.getValue(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		Double d = (Double) propertyValue;
		doubleValueContainer.setValue(id, d);
	}

	@Override
	public void incrementCapacity(int count) {
		super.incrementCapacity(count);
		doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
	}

}
