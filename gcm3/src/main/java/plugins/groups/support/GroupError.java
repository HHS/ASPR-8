package plugins.groups.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum GroupError implements ContractError {
	
	NEGATIVE_GROUP_ID("group id is negative"),
	NEGATIVE_GROUP_COUNT("group count is negative"),
	NULL_GROUP_INITIALIZATION_DATA("Null group initialization data"),	
	NULL_GROUP_DATA_MANAGER("Null group data manager"),
	DUPLICATE_GROUP_MEMBERSHIP("Person was previously assigned to group"),
	DUPLICATE_GROUP_TYPE("Duplicate group type"),
	DUPLICATE_GROUP_PROPERTY_VALUE_ASSIGNMENT("Duplicate group property value assignment"),
	DUPLICATE_GROUP_PROPERTY_DEFINITION("Duplicate group property definition"),
	DUPLICATE_GROUP_ID("Duplicate group id"),
	DUPLICATE_GROUP_PROPERTY_ID("Duplicate group property id"),
	MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION("Data used to form an enumerated distribution for group sampling was malformed"),
	NON_GROUP_MEMBERSHIP("Person is not currently assigned to group"),
	NULL_GROUP_CONSTRUCTION_INFO("Null group construction info"),
	NULL_GROUP_ID("Null group id"),
	NULL_GROUP_PROPERTY_ID("Null group property id"),
	NULL_GROUP_PROPERTY_VALUE("Null group property value"),
	NULL_PROPERTY_DEFINITION("Null property defintion"),
	NULL_GROUP_SAMPLER("Null group sampler"),
	NULL_GROUP_TYPE_ID("Null group type id"),
	UNKNOWN_GROUP_ID("Unknown group id"),
	UNKNOWN_GROUP_PROPERTY_ID("Unknown group property id"),
	UNKNOWN_GROUP_TYPE_ID("Unknown group type id"),	
	INSUFFICIENT_GROUP_PROPERTY_VALUE_ASSIGNMENT("A group property definition default value is null and not replaced with sufficient property value assignments");
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
