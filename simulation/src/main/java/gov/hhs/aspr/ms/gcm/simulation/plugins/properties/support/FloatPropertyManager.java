package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.arraycontainers.FloatValueContainer;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Float property values
 * into a float[]-based data structure.
 */
public final class FloatPropertyManager implements IndexedPropertyManager {
	/*
	 * A container, indexed by person id, that stores Double values as an array of
	 * float.
	 */
	private FloatValueContainer floatValueContainer;

	/**
	 * Constructs this FloatPropertyManager.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
	 *                           if the property definition is null</li>
	 *                           <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *                           if the property definition's type is not
	 *                           Boolean</li>
	 *                           </ul>
	 */
	public FloatPropertyManager(PropertyDefinition propertyDefinition,
			Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (propertyDefinition.getType() != Float.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,
					"Requires a property definition with float type");
		}
		Float defaultValue = 0F;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = (Float) propertyDefinition.getDefaultValue().get();
		}
		floatValueContainer = new FloatValueContainer(defaultValue, indexIteratorSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Float result = floatValueContainer.getValue(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Float f = (Float) propertyValue;
		floatValueContainer.setValue(id, f);
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		floatValueContainer.setCapacity(floatValueContainer.getCapacity() + count);
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
		builder.append("FloatPropertyManager [floatValueContainer=");
		builder.append(floatValueContainer);
		builder.append("]");
		return builder.toString();
	}

}
