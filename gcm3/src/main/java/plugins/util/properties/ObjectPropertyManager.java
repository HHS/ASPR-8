package plugins.util.properties;

import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.util.properties.arraycontainers.ObjectValueContainer;

/**
 * Implementor of IndexedPropertyManager that stores Object property values in an
 * Object array based data structure.
 * 
 * @author Shawn Hatch
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
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 */
	public ObjectPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);

		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);			
		}

		defaultValue = propertyDefinition.getDefaultValue().get();
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
