package plugins.people.support;

import nucleus.util.ContractError;
import nucleus.util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum PersonError implements ContractError {
	// person
	NULL_PERSON_DATA_MANAGER("Null person data manager"),	
	NULL_AUXILIARY_DATA("Null auxiliary data"),
	NULL_BULK_PERSON_CONTRUCTION_DATA("Null bulk person contruction data"),
	NULL_PERSON_CONTRUCTION_DATA("Null person contruction data"),
	NULL_PERSON_ID("Null person id"),
	UNKNOWN_PERSON_ID("Unknown person id"),
	DUPLICATE_PERSON_ID("Duplicate person addition"),
	NON_ONE_TO_ONE_MAPPING("Mapping of initial data person ids to simulation person is not one-to-one"),

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
