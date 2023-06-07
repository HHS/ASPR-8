package plugins.util.properties;

import plugins.util.properties.arraycontainers.BooleanContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Boolean property values
 * into a bit-based data structure.
 * 
 *
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
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE}
	 *             if the property definition's type is not Boolean</li>
	 */
	
	public BooleanPropertyManager( PropertyDefinition propertyDefinition, int initialSize) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		
		if (initialSize < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INITIAL_SIZE);
		}
		if (propertyDefinition.getType() != Boolean.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "Requires a property definition with Boolean type ");
		}
		boolean defaultValue = false;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = (Boolean)propertyDefinition.getDefaultValue().get();			
		}		

		boolContainer = new BooleanContainer(defaultValue, initialSize);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getPropertyValue(int id) {
		if(id<0) {
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
