package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum GroupError implements ContractError {

	DUPLICATE_GROUP_MEMBERSHIP("Person was previously assigned to group"),//
	DUPLICATE_GROUP_TYPE("Duplicate group type"),//
	GROUP_MEMBERSHIP_ASYMMETRY("Person-group relationships are asymmetric"),//
	INCORRECT_GROUP_TYPE_ID("incorrect group type id for a group"),//
	MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION("Data used to form an enumerated distribution for group sampling was malformed"),//
	NEGATIVE_GROUP_ID("group id is negative"), NEGATIVE_GROUP_COUNT("group count is negative"),//
	NEXT_GROUP_ID_TOO_SMALL("The next group id must exceed all extant group ids"),//
	NON_GROUP_MEMBERSHIP("Person is not currently assigned to group"),//
	NULL_GROUP_CONSTRUCTION_INFO("Null group construction info"),//
	NULL_GROUP_ID("Null group id"),//
	NULL_GROUP_INITIALIZATION_DATA("Null group initialization data"),//
	NULL_GROUP_PLUGIN_DATA("null groupsplugin data"),//
	NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA("Null group population report plugin data"),//
	NULL_GROUP_PROPERTY_REPORT_PLUGIN_DATA("Null group property report plugin data"),//
	NULL_GROUP_SAMPLER("Null group sampler"),//
	NULL_GROUP_TYPE_ID("Null group type id"),//
	UNKNOWN_GROUP_ID("Unknown group id"),//
	UNKNOWN_GROUP_TYPE_ID("Unknown group type id"),//
	;

	private final String description;

	private GroupError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
