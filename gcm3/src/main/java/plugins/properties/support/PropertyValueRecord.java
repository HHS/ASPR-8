package plugins.properties.support;

import nucleus.SimulationContext;

/**
 * A utility class for holding the value and assignment time for a property. On
 * value assignment, this PropertyValueRecord records the current simulation
 * time.
 */
public class PropertyValueRecord {

	private Object propertyValue;
	private double assignmentTime;
	private final SimulationContext simulationContext;

	public PropertyValueRecord(SimulationContext simulationContext) {
		this.simulationContext = simulationContext;
	}

	/**
	 * Returns the last assigned value
	 */
	public Object getValue() {		
		return propertyValue;
	}

	/**
	 * Sets the current value and records the assignment time
	 */
	public void setPropertyValue(Object propertyValue) {
		this.propertyValue = propertyValue;
		assignmentTime = simulationContext.getTime();
	}

	/**
	 * Returns the time of the last assignment
	 */
	public double getAssignmentTime() {
		return assignmentTime;
	}
}