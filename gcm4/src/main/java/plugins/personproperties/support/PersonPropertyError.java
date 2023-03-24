package plugins.personproperties.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum PersonPropertyError implements ContractError {
	NULL_PERSON_PROPERTY_PLUGN_DATA("Null person property plugin data"), //
	NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA("Null person property interaction report plugin data"), //
	NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA("Null person property report plugin data"),
	NULL_PERSON_PROPERTY_DATA_MANAGER("Null person property data manager"), //
	UNKNOWN_PERSON_ID("Unknown person id"), //
	PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED("Property assignment time not actively tracked"),//
	PROPERTY_ASSIGNMENT_FOR_NON_ADDED_PERSON("Property assignment for non added person"),
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
