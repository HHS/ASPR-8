package plugins.regions.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum RegionError implements ContractError {
	DUPLICATE_PERSON_ADDITION("Duplicate person region addition"),
	DUPLICATE_REGION_ID("Duplicate region id"),
	MISSING_REGION_ASSIGNMENT("Region assignment is missing"),
	NULL_AUXILIARY_DATA("Null auxiliary data"),
	NULL_REGION_CONSTRUCTION_DATA("Null region construction data"),
	NULL_REGION_ID("Null region id"),
	NULL_REGION_PLUGIN_DATA("Null region plugin data"),
	NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION("Null region property definition initialization"),
	NULL_TIME_TRACKING_POLICY("Null time tracking policy"),
	REGION_ARRIVAL_TIMES_NOT_TRACKED("Person region arrival times not actively tracked"),
	UNKNOWN_REGION_ID("Unknown region id"),
	;
	

	
	private final String description;

	private RegionError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
