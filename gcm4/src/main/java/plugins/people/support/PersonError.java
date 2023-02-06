package plugins.people.support;

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
	UNKNOWN_PERSON_ID("Unknown person id"),
	DUPLICATE_PERSON_ID("Duplicate person addition"),
	NON_ONE_TO_ONE_MAPPING("Mapping of initial data person ids to simulation person is not one-to-one"),
	NULL_PEOPLE_PLUGIN_DATA("null people plugin data"),

	NULL_SUGGESTED_POPULATION_SIZE("Scenario identifier is null"),
	NEGATIVE_SUGGGESTED_POPULATION("Suggested population size is negative"),
	NEGATIVE_GROWTH_PROJECTION("Growth projection count is negative"),
	DUPLICATE_SUGGESTED_POPULATION_SIZE_ASSIGNMENT("Duplicate assignment of suggested population size");

	
	
	private final String description;

	private PersonError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
