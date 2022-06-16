package plugins.personproperties.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum PersonPropertyError implements ContractError {
	NULL_PERSON_PROPERTY_PLUGN_DATA("Null person property plugin data"), //
	NULL_PERSON_PROPERTY_DATA_MANAGER("Null person property data manager"), //
	UNKNOWN_PERSON_ID("Unknown person id"), //
	PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED("Property assignment time not actively tracked"),//
	;

	private final String description;

	private PersonPropertyError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
