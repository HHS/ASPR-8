package plugins.properties.support;

import nucleus.SimulationContext;
import util.ContractException;
import util.arraycontainers.BooleanContainer;

/**
 * Implementor of IndexedPropertyManager that compresses Boolean property values
 * into a bit-based data structure.
 * 
 * @author Shawn Hatch
 *
 */
public final class BooleanPropertyManager extends AbstractIndexedPropertyManager {

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
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 * 
	 */
	public BooleanPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		super(simulationContext, propertyDefinition, initialSize);
		if (propertyDefinition.getType() != Boolean.class) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_IMPROPER_TYPE, "Requires a property definition with Boolean type ");
		}
		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
		boolean defaultValue = (Boolean) propertyDefinition.getDefaultValue().get();

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
		super.setPropertyValue(id, propertyValue);
		Boolean b = (Boolean) propertyValue;
		boolContainer.set(id, b.booleanValue());
	}

	@Override
	public void incrementCapacity(int count) {
		super.incrementCapacity(count);
		boolContainer.expandCapacity(count);
	}

}
