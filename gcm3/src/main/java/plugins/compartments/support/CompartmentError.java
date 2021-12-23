package plugins.compartments.support;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum CompartmentError implements ContractError {
	COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED("Person compartment arrival times not actively tracked"),
	DUPLICATE_COMPARTMENT_INITIAL_BEHAVIOR_ASSIGNMENT("Duplicate compartment agent initial behavior assignment"),
	DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION_ASSIGNMENT("Duplicate compartment property definition assignment"),
	DUPLICATE_COMPARTMENT_PROPERTY_VALUE("Duplicate compartment property value"),
	DUPLICATE_PERSON_COMPARTMENT_ASSIGNMENT("Duplicate person compartment assignment"),
	DUPLICATE_TIME_TRACKING_POLICY("Duplicate time tracking policy"),
	INSUFFICIENT_COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT("A compartment property definition default value is null and not replaced with sufficient property value assignments"),
	MISSING_COMPARTMENT_ASSIGNMENT("Compartment assignment is missing"),
	NULL_COMPARTMENT_ID("Null compartment id"),
	NULL_COMPARTMENT_INITIAL_BEHAVIOR_SUPPLIER("Null compartment initia behavior supplier"),
	NULL_COMPARTMENT_PROPERTY_DEFINITION("Null compartment property definition"),
	NULL_COMPARTMENT_PROPERTY_ID("Null compartment property id"),
	NULL_COMPARTMENT_PROPERTY_VALUE("Null compartment value"),
	NULL_TIME_TRACKING_POLICY("Null time tracking policy"),
	SAME_COMPARTMENT("Cannot move a person into the compartment they are already occupying"),
	UNKNOWN_COMPARTMENT_ID("Unknown compartment id"),
	UNKNOWN_COMPARTMENT_PROPERTY_ID("Unknown compartment property id"),
	NULL_COMPARTMENT_DATA_MANAGER("null compartment data manager"),
	;

	private final String description;

	private CompartmentError(final String description) {
		this.description = description;
	}

	/**
	 * Returns the unique, non-empty text description of the error
	 */
	@Override
	public String getDescription() {
		return description;
	}
}
