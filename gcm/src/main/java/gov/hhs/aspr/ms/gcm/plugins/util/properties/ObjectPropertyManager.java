package gov.hhs.aspr.ms.gcm.plugins.util.properties;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.ObjectValueContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that stores Object property values in
 * an Object array based data structure.
 */
public final class ObjectPropertyManager implements IndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores Objects as an array.
	 */
	private ObjectValueContainer objectValueContainer;
	private final Object defaultValue;

	/**
	 * Constructs this IntPropertyManager.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE}
	 *                           if the initial size is negative</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
	 *                           if the property definition is null</li>
	 *                           </ul>
	 */
	public ObjectPropertyManager(PropertyDefinition propertyDefinition,
			Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = propertyDefinition.getDefaultValue().get();
		} else {
			defaultValue = null;
		}

		objectValueContainer = new ObjectValueContainer(defaultValue, indexIteratorSupplier);
	}

	@Override
	public <T> T getPropertyValue(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		return objectValueContainer.getValue(id);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		objectValueContainer.setValue(id, propertyValue);
	}

	@Override
	public void removeId(int id) {
		// dropping reference to the currently stored value for potential
		// garbage collection
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		objectValueContainer.setValue(id, defaultValue);
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		objectValueContainer.setCapacity(objectValueContainer.getCapacity() + count);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObjectPropertyManager [objectValueContainer=");
		builder.append(objectValueContainer);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append("]");
		return builder.toString();
	}

}
