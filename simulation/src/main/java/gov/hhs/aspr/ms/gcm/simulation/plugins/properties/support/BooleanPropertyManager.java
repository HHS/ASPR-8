package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.arraycontainers.BooleanContainer;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Boolean property values
 * into a bit-based data structure.
 */
public final class BooleanPropertyManager implements IndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores boolean values as bits.
	 */
	private BooleanContainer boolContainer;

	/**
	 * Constructs this BooleanPropertyManager.
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
	public BooleanPropertyManager(PropertyDefinition propertyDefinition,
			Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (propertyDefinition.getType() != Boolean.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE,
					"Requires a property definition with Boolean type ");
		}
		boolean defaultValue = false;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = (Boolean) propertyDefinition.getDefaultValue().get();
		}

		boolContainer = new BooleanContainer(defaultValue, indexIteratorSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Boolean result = boolContainer.get(id);
		return (T) result;
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		Boolean b = (Boolean) propertyValue;
		boolContainer.set(id, b.booleanValue());
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		boolContainer.expandCapacity(count);
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
		builder.append("BooleanPropertyManager [boolContainer=");
		builder.append(boolContainer);
		builder.append("]");
		return builder.toString();
	}

}
