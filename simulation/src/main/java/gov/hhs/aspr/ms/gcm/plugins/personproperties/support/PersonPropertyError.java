package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PersonPropertyError implements ContractError {

	NON_FINITE_TIME("Non-finite time value"),//
	NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA("Null person property interaction report plugin data"), //
	NULL_PERSON_PROPERTY_PLUGN_DATA("Null person property plugin data"), //
	NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA("Null person property report plugin data"),//
	NULL_TIME("Null time"), //
	PROPERTY_ASSIGNMENT_TIME_EXCEEDS_SIM_TIME("Property assignment time exceeds current simulation time"),//
	PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED("Property assignment time not actively tracked"), //
	PROPERTY_DEFAULT_TIME_EXCEEDS_SIM_TIME("Property default time exceeds current simulation time"),//
	PROPERTY_TIME_PRECEDES_DEFAULT("A property value assignment time precedes the default assignment time for the associated property"),//
	UNKNOWN_PERSON_HAS_PROPERTY_ASSIGNMENT_TIME("Unknown person has property assignment time"),//
	UNKNOWN_PERSON_HAS_PROPERTY_VALUE_ASSIGNMENT("Unknown person has property value assignment"),//
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
