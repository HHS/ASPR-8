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
	NULL_PERSON_PROPERTY_PLUGN_DATA("Null person property plugin data"),//
	NULL_PERSON_PROPERTY_DATA_MANAGER("Null person property data manager"),//
	NULL_PERSON_PROPERTY_ID("Null person property id"),//	
	UNKNOWN_PERSON_ID("Unknow person id"), //
	NULL_PERSON_PROPERTY_DEFINITION("Null person property definition"),//
	NULL_PERSON_PROPERTY_VALUE("Null person property value"),//
	PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED("Property assignment time not actively tracked"),//
	UNKNOWN_PERSON_PROPERTY_ID("Unknown person property id"),//	
	DUPLICATE_PERSON_PROPERTY_DEFINITION("Duplicate person property definition"),//
	DUPLICATE_PERSON_PROPERTY_VALUE_ASSIGNMENT("Duplicate person property value assignment");//

	private final String description;

	private PersonPropertyError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
