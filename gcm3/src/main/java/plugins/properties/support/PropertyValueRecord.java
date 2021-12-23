package plugins.properties.support;

import nucleus.Context;

/**
 * A utility class for holding the value and assignment time for a property. On
 * value assignment, this PropertyValueRecord records the current simulation
 * time.
 */
public class PropertyValueRecord {

	private Object propertyValue;
	private double assignmentTime;
	private final Context context;

	public PropertyValueRecord(Context context) {
		this.context = context;
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
		assignmentTime = context.getTime();
	}

	/**
	 * Returns the time of the last assignment
	 */
	public double getAssignmentTime() {
		return assignmentTime;
	}
}