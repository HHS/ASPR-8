package plugins.util.properties;

import nucleus.SimulationContext;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
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

	/*
	 * Contains the assignment times for this property value. Subject to
	 * tracking policy.
	 */
	private DoubleValueContainer timeTrackingContainer;

	/*
	 * The time tracking policy.
	 */
	private final boolean trackTime;

	private SimulationContext simulationContext;

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
	public AbstractIndexedPropertyManager(SimulationContext simulationContext, PropertyDefinition propertyDefinition, int initialSize) {
		this.simulationContext = simulationContext;
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		trackTime = propertyDefinition.getTimeTrackingPolicy() == TimeTrackingPolicy.TRACK_TIME;
		if (initialSize < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INITIAL_SIZE);
		}
		if (trackTime) {
			timeTrackingContainer = new DoubleValueContainer(simulationContext.getTime(), initialSize);
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
		if (trackTime) {
			timeTrackingContainer.setValue(id, simulationContext.getTime());
		}
	}

	@Override
	public final double getPropertyTime(int id) {
		if (id < 0) {
			throw new ContractException(PropertyError.NEGATIVE_INDEX);
		}

		if (trackTime) {
			return timeTrackingContainer.getValue(id);
		}

		throw new ContractException(PropertyError.TIME_TRACKING_OFF);

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
		if (trackTime) {
			timeTrackingContainer.setCapacity(timeTrackingContainer.getCapacity() + count);
		}
	}
}
