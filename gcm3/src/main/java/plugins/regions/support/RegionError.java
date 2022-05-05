package plugins.regions.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum RegionError implements ContractError {

	

	NULL_REGION_ID("Null region id"),
	DUPLICATE_REGION_ID("Duplicate region id"),
	UNKNOWN_REGION_ID("Unknown region id"),
	MISSING_REGION_ASSIGNMENT("Region assignment is missing"),
	NULL_REGION_PROPERTY_ID("Null region property id"),
	UNKNOWN_REGION_PROPERTY_ID("Unknown region property id"),
	NULL_REGION_PROPERTY_DEFINITION("Null region property definition"),
	NULL_REGION_PLUGIN_DATA("Null region plugin data"),
	NULL_REGION_PROPERTY_VALUE("Null region property value"),	
	DUPLICATE_PERSON_REGION_ASSIGNMENT("Duplicate person region assignment"),
	DUPLICATE_PERSON_ADDITION("Duplicate person region addition"),
	DUPLICATE_REGION_PROPERTY_VALUE("Duplicate region property value"),	
	DUPLICATE_REGION_PROPERTY_DEFINITION_ASSIGNMENT("Duplicate region property definition assignment"),

	
	NULL_TIME_TRACKING_POLICY("Null time tracking policy"),
	DUPLICATE_TIME_TRACKING_POLICY("Duplicate time tracking policy"),

	INSUFFICIENT_REGION_PROPERTY_VALUE_ASSIGNMENT("A regiont property definition default value is null and not replaced with sufficient property value assignments"),

	REGION_ARRIVAL_TIMES_NOT_TRACKED("Person region arrival times not actively tracked");
	

	
	private final String description;

	private RegionError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
