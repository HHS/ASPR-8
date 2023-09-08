package gov.hhs.aspr.ms.gcm.plugins.util.properties;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.DoubleValueContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Double property values
 * into a double[]-based data structure.
 */
public final class DoublePropertyManager implements IndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores Double values as an array of
	 * double.
	 */
	private DoubleValueContainer doubleValueContainer;

	/**
	 * Constructs this DoublePropertyManager.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE}
	 *                           if the initial size is negative</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
	 *                           if the property definition is null</li>
	 *                           <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *                           if the property definition's type is not
	 *                           Double</li>
	 *                           </ul>
	 */
	public DoublePropertyManager(PropertyDefinition propertyDefinition,
			Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (propertyDefinition.getType() != Double.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,
					"Requires a property definition with Double type ");
		}
		Double defaultValue = 0D;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = (Double) propertyDefinition.getDefaultValue().get();
		}
		doubleValueContainer = new DoubleValueContainer(defaultValue, indexIteratorSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Double result = doubleValueContainer.getValue(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Double d = (Double) propertyValue;
		doubleValueContainer.setValue(id, d);
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		doubleValueContainer.setCapacity(doubleValueContainer.getCapacity() + count);
	}

	@Override
	public void removeId(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DoublePropertyManager [doubleValueContainer=");
		builder.append(doubleValueContainer);
		builder.append("]");
		return builder.toString();
	}

}
