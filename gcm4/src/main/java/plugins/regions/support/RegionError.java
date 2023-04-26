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
	NON_FINITE_TIME("Not a finite time value"),
	NULL_AUXILIARY_DATA("Null auxiliary data"),
	NULL_REGION_CONSTRUCTION_DATA("Null region construction data"),
	NULL_REGION_ID("Null region id"),
	NULL_REGION_PLUGIN_DATA("Null region plugin data"),
	NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION("Null region property definition initialization"),
	NULL_TIME_TRACKING_POLICY("Null time tracking policy"),
	REGION_ARRIVAL_TIMES_NOT_TRACKED("Person region arrival times not actively tracked"),
	REGION_ARRIVAL_TIMES_MISMATCHED("Person region arrival times are actively tracked, but region assignment and region arrival assignments do not align"),
	UNKNOWN_REGION_ID("Unknown region id"),
	REGION_ARRIVAL_TIME_EXCEEDS_SIM_TIME("A person region arrival time exceeds the current simulation time"),
	UNKNOWN_REGION_ARRIVAL_TIME("Unknown region arrival time"),
	PERSON_ARRIVAL_DATA_PRESENT("Person arrival times were recorded while the tracking policy is false"),
	NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA("Null region property report plugin data"),
	NULL_REGION_TRANSFER_REPORT_PLUGIN_DATA("Null region transfer report plugin data"),
	
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
