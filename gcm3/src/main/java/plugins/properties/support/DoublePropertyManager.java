package plugins.properties.support;

import nucleus.Context;
import util.ContractException;
import util.arraycontainers.DoubleValueContainer;

/**
 * Implementor of IndexedPropertyManager that compresses Double property values
 * into a double[]-based data structure.
 * 
 * @author Shawn Hatch
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
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 * 
	 */
	public DoublePropertyManager(Context context, PropertyDefinition propertyDefinition, int initialSize) {
		super(context, propertyDefinition, initialSize);

		if (propertyDefinition.getType() != Double.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "Requires a property definition with Double type ");
		}

		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}

		Double defaultValue = (Double) propertyDefinition.getDefaultValue().get();

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
