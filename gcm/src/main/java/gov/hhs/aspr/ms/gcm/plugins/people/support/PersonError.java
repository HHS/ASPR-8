package gov.hhs.aspr.ms.gcm.plugins.people.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum PersonError implements ContractError {
	// person
	NEGATIVE_PERSON_ID("Negative person id"),
	NULL_PERSON_DATA_MANAGER("Null person data manager"),	
	NULL_AUXILIARY_DATA("Null auxiliary data"),
	NULL_PERSON_CONSTRUCTION_DATA("Null person construction data"),
	NULL_PERSON_ID("Null person id"),
	NULL_PERSON_RANGE("Null person range"),
	UNKNOWN_PERSON_ID("Unknown person id"),
	DUPLICATE_PERSON_ID("Duplicate person addition"),
	ILLEGAL_PERSON_RANGE("Illegal person range"),
	INVALID_PERSON_COUNT("The person count must exceed the highest person range value"),
	NEGATIVE_PERSON_COUNT("Negative person count"),
	PERSON_ASSIGNMENT_TIME_IN_FUTURE("The person assignment time from the plugin data is greater than the current time in the simulation"),
	NON_ONE_TO_ONE_MAPPING("Mapping of initial data person ids to simulation person is not one-to-one"),
	NULL_PEOPLE_PLUGIN_DATA("null people plugin data"),

	NULL_SUGGESTED_POPULATION_SIZE("Scenario identifier is null"),
	NEGATIVE_SUGGGESTED_POPULATION("Suggested population size is negative"),
	NEGATIVE_GROWTH_PROJECTION("Growth projection count is negative");
	
	private final String description;

	private PersonError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
