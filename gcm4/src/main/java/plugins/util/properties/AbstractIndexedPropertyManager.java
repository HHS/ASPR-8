package plugins.util.properties;

import util.errors.ContractException;

/**
 * The abstract base class for all IndexedPropertyManager implementors.
 * 
 * It implements all property time recording and reverse mapping of property
 * values to people. Its implementation of these methods is final.
 * 
 * It also implements setPropertyValue() and descendant classes are expected to
 * invoke super.setPropertyValue()
 * 
 * Finally, it leaves the implementation of getPropertyValue() to its descendant
 * classes
 * 
 *
 */
public abstract class AbstractIndexedPropertyManager implements IndexedPropertyManager {

	/**
	 * Constructs an AbstractPropertyManger. Establishes the time tracking and
	 * map option policies from the environment. Establishes the property value
	 * to people mapping if the MapOption is not NONE.
	 * 
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NEGATIVE_INITIAL_SIZE} if the
	 *             initial size is negative</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 */
	public AbstractIndexedPropertyManager(PropertyDefinition propertyDefinition, int initialSize) {

		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		
		if (initialSize < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INITIAL_SIZE);
		}
		
	}

	@Override
	public void setPropertyValue(int id, Object propertyValue) {
		/*
		 * Record the time value if we are tracking assignment times.
		 */
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
		
	}


	@Override
	public void removeId(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}
	}

	@Override
	public void incrementCapacity(int count) {
		if (count < 0) {
			throw new ContractException(PropertyError.NEGATIVE_CAPACITY_INCREMENT);
		}
	}
}
