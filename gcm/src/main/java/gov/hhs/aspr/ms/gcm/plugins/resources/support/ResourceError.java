package gov.hhs.aspr.ms.gcm.plugins.resources.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum ResourceError implements ContractError {

	INSUFFICIENT_RESOURCES_AVAILABLE("Resource level is insufficient for transaction amount"),
	DUPLICATE_RESOURCE_ID("Duplicate resource"),
	DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT("Duplicate region resource level assignment"),
	NEGATIVE_RESOURCE_AMOUNT("Resource amount is negative"), NULL_AMOUNT("Null amount"), NULL_TIME("Null time"),
	NULL_RESOURCE_ID("Null resource id"), NULL_RESOURCE_DATA_MANAGER("Null resource data manager"),
	NULL_RESOURCE_PLUGIN_DATA("Null resource plugin data"),
	NULL_PERSON_RESOURCE_REPORT_PLUGIN_DATA("Null person resource report plugin data"),
	NULL_RESOURCE_PROPERTY_REPORT_PLUGIN_DATA("Null resource property report plugin data"),
	NULL_RESOURCE_REPORT_PLUGIN_DATA("Null resource report plugin data"),
	NULL_TIME_TRACKING_POLICY("Null time tracking policy"),
	REFLEXIVE_RESOURCE_TRANSFER("Cannot transfer resources from a region to itself"),
	RESOURCE_ARITHMETIC_EXCEPTION("Resource arithmetic resulting in underflow/overflow"),
	RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED("Resource assignment time not actively tracked"),
	RESOURCE_CREATION_TIME_EXCEEDS_SIM_TIME("Resource creation time exceeds current simulation time"),
	RESOURCE_ASSIGNMENT_TIME_PRECEEDS_RESOURCE_CREATION_TIME(
			"Resource assignment time preceeds resource creation time"),
	RESOURCE_ASSIGNMENT_TIME_EXCEEDS_SIM_TIME("Resource assignment time exceeds current simulation time"),
	UNKNOWN_RESOURCE_ID("Unknown resource id"),

	;

	private final String description;

	private ResourceError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
