package gov.hhs.aspr.ms.gcm.plugins.people.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PersonError implements ContractError {
	
	ILLEGAL_PERSON_RANGE("Illegal person range"),//
	INVALID_PERSON_COUNT("The person count must exceed the highest person range value"),//
	NEGATIVE_GROWTH_PROJECTION("Growth projection count is negative"),//
	NEGATIVE_PERSON_COUNT("Negative person count"),//
	NEGATIVE_PERSON_ID("Negative person id"),//
	NULL_AUXILIARY_DATA("Null auxiliary data"), //
	NULL_PEOPLE_PLUGIN_DATA("null people plugin data"),//
	NULL_PERSON_CONSTRUCTION_DATA("Null person construction data"),//
	NULL_PERSON_ID("Null person id"), //
	NULL_PERSON_RANGE("Null person range"),// 
	PERSON_ASSIGNMENT_TIME_IN_FUTURE("The person assignment time from the plugin data is greater than the current time in the simulation"),//
	UNKNOWN_PERSON_ID("Unknown person id"),//
	
	;

	private final String description;

	private PersonError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
