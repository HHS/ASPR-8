package plugins.util.properties;

import plugins.util.properties.arraycontainers.ObjectValueContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that stores Object property values in an
 * Object array based data structure.
 * 
 *
 */
public final class ObjectPropertyManager extends AbstractIndexedPropertyManager {

	/*
	 * A container, indexed by person id, that stores Objects as an array.
	 */
	private ObjectValueContainer objectValueContainer;
	private final Object defaultValue;

	/**
	 * Constructs this IntPropertyManager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li> 
	 * 
	 */
	public ObjectPropertyManager(PropertyDefinition propertyDefinition, int initialSize) {
		super(propertyDefinition, initialSize);
		
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = propertyDefinition.getDefaultValue().get();			
		}else {
			defaultValue = null;	
		}

		
		objectValueContainer = new ObjectValueContainer(defaultValue, initialSize);
	}

	@Override
	public <T> T getPropertyValue(int id) {
		if(id<0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		return objectValueContainer.getValue(id);
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		super.setPropertyValue(id, propertyValue);
		objectValueContainer.setValue(id, propertyValue);
	}

	@Override
	public void removeId(int id) {
		// dropping reference to the currently stored value for potential
		// garbage collection
		super.removeId(id);
		objectValueContainer.setValue(id, defaultValue);
	}
	
	@Override
	public void incrementCapacity(int count) {
		super.incrementCapacity(count);
		objectValueContainer.setCapacity(objectValueContainer.getCapacity()+count);		
	}

}
