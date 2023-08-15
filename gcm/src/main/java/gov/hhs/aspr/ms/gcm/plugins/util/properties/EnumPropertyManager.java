package gov.hhs.aspr.ms.gcm.plugins.util.properties;

import java.util.Iterator;
import java.util.function.Supplier;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.arraycontainers.EnumContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Enum property values
 * into a byte-based data structure of the various int-like primitives.
 * 
 *
 */
public final class EnumPropertyManager implements IndexedPropertyManager {
	/*
	 * The storage container.
	 */
	private EnumContainer enumContainer;

	/**
	 * Constructs this EnumPropertyManager.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE} if the
	 *             property definition's type is not an enumeration</li>
	 */
	public EnumPropertyManager(PropertyDefinition propertyDefinition, Supplier<Iterator<Integer>> indexIteratorSupplier) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		
		Object defaultValue = null;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = propertyDefinition.getDefaultValue().get();
		}

		if (!Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "cannot construct from class " + propertyDefinition.getClass().getName());
		}

		enumContainer = new EnumContainer(propertyDefinition.getType(),defaultValue, indexIteratorSupplier);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if(id<0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		return (T) enumContainer.getValue(id);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		enumContainer.setValue(id, propertyValue);
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
		enumContainer.setCapacity(enumContainer.getCapacity() + count);
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
		builder.append("EnumPropertyManager [enumContainer=");
		builder.append(enumContainer);
		builder.append("]");
		return builder.toString();
	}
	
	

}
