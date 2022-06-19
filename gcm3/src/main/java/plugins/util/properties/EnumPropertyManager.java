package plugins.util.properties;

import nucleus.SimulationContext;
import plugins.util.properties.arraycontainers.EnumContainer;
import util.errors.ContractException;

/**
 * Implementor of IndexedPropertyManager that compresses Enum property values
 * into a byte-based data structure of the various int-like primitives.
 * 
 * @author Shawn Hatch
 *
 */
public final class EnumPropertyManager extends AbstractIndexedPropertyManager {
	/*
	 * The storage container.
	 */
	private EnumContainer enumContainer;

	/**
	 * Constructs this EnumPropertyManager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_IMPROPER_TYPE} if the
	 *             property definition's type is not an enumeration</li>
	 * 
	 */
	public EnumPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);

		
		Object defaultValue = null;
		if (propertyDefinition.getDefaultValue().isPresent()) {
			defaultValue = propertyDefinition.getDefaultValue().get();
		}

		if (!Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "cannot construct from class " + propertyDefinition.getClass().getName());
		}

		enumContainer = new EnumContainer(propertyDefinition.getType(),defaultValue, initialSize);
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
		super.setPropertyValue(id, propertyValue);
		enumContainer.setValue(id, propertyValue);
	}

	@Override
	public void incrementCapacity(int count) {
		super.incrementCapacity(count);
		enumContainer.setCapacity(enumContainer.getCapacity() + count);
	}

}
