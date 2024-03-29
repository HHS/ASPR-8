package gov.hhs.aspr.ms.gcm.plugins.properties.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PropertyError implements ContractError {

	INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT(
			"A property definition lacks a default value and there are not sufficient property value assignments to cover the missing default value"), //
	NULL_PROPERTY_ID("Null property id"), NULL_PROPERTY_DEFINITION("Null property definition"), //
	NULL_PROPERTY_DEFINITION_INITIALIZATION("Null property definition initialization"), //
	NULL_PROPERTY_VALUE("Null property value"),
	DUPLICATE_PROPERTY_DEFINITION("Duplicate assignment of a property definition to a property id"),
	UNKNOWN_PROPERTY_ID("Unknown property id"),
	INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"), //
	INCOMPATIBLE_TIME("Property assignment time is less than property definition creation time"), //
	INCOMPATIBLE_DEF_TIME("Property definition creation time exceeds simulation time"), //
	DUPLICATE_PROPERTY_VALUE_ASSIGNMENT("Duplicate property value assignment"),
	PROPERTY_CREATION_TIME_NOT_CURRENT("A property creation time does not agree with the current time"),

	NULL_PROPERTY_TYPE("Type for property definition is null"), //
	IMMUTABLE_VALUE("This property is defined as immutable"), //
	INCOMPATIBLE_DEFAULT_VALUE("Default value is incompatible with the class type"), //
	PROPERTY_DEFINITION_IMPROPER_TYPE("Property definition has improper data type"), //

	NEGATIVE_INITIAL_SIZE("Negative initial size"), //
	NEGATIVE_CAPACITY_INCREMENT("Negative capacity increment"), //
	NEGATIVE_INDEX("Negative index"), //

	NULL_TIME_TRACKING_POLICY("Time tracking policy is null"), //
	TIME_TRACKING_OFF("Time tracking is off"),//

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
