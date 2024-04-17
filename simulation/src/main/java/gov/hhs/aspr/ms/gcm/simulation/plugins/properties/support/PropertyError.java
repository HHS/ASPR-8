package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PropertyError implements ContractError {

	DUPLICATE_PROPERTY_DEFINITION("Duplicate assignment of a property definition to a property id"),//
	DUPLICATE_PROPERTY_VALUE_ASSIGNMENT("Duplicate property value assignment"),//
	IMMUTABLE_VALUE("This property is defined as immutable"), //
	INCOMPATIBLE_DEF_TIME("Property definition creation time exceeds simulation time"), //
	INCOMPATIBLE_DEFAULT_VALUE("Default value is incompatible with the class type"), //
	INCOMPATIBLE_TIME("Property assignment time is less than property definition creation time"), //
	INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"), //
	INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT("A property definition lacks a default value and there are not sufficient property value assignments to cover the missing default value"), //
	NEGATIVE_CAPACITY_INCREMENT("Negative capacity increment"), //
	NEGATIVE_INDEX("Negative index"), //
	NULL_PROPERTY_DEFINITION_INITIALIZATION("Null property definition initialization"), //
	NULL_PROPERTY_ID("Null property id"), NULL_PROPERTY_DEFINITION("Null property definition"), //
	NULL_PROPERTY_TYPE("Type for property definition is null"), //
	NULL_PROPERTY_VALUE("Null property value"),//
	PROPERTY_DEFINITION_IMPROPER_TYPE("Property definition has improper data type"), //
	TIME_TRACKING_OFF("Time tracking is off"),//
	UNKNOWN_PROPERTY_ID("Unknown property id"),//

	;

	private final String description;

	private PropertyError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
