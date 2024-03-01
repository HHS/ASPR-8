package gov.hhs.aspr.ms.gcm.plugins.resources.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum ResourceError implements ContractError {

	DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT("Duplicate region resource level assignment"),
	DUPLICATE_RESOURCE_ID("Duplicate resource"),
	INSUFFICIENT_RESOURCES_AVAILABLE("Resource level is insufficient for transaction amount"),
	NEGATIVE_RESOURCE_AMOUNT("Resource amount is negative"),
	NULL_PERSON_RESOURCE_REPORT_PLUGIN_DATA("Null person resource report plugin data"),
	NULL_RESOURCE_ID("Null resource id"),
	NULL_RESOURCE_PLUGIN_DATA("Null resource plugin data"),
	NULL_RESOURCE_PROPERTY_REPORT_PLUGIN_DATA("Null resource property report plugin data"),
	NULL_RESOURCE_REPORT_PLUGIN_DATA("Null resource report plugin data"),
	NULL_TIME("Null time"),
	REFLEXIVE_RESOURCE_TRANSFER("Cannot transfer resources from a region to itself"),
	RESOURCE_ARITHMETIC_EXCEPTION("Resource arithmetic resulting in underflow/overflow"),
	RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED("Resource assignment time not actively tracked"),
	RESOURCE_ASSIGNMENT_TIME_PRECEEDS_RESOURCE_CREATION_TIME("Resource assignment time preceeds resource creation time"),
	RESOURCE_CREATION_TIME_EXCEEDS_SIM_TIME("Resource creation time exceeds current simulation time"),
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
