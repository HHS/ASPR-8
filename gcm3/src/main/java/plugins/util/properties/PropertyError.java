package plugins.util.properties;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum PropertyError implements ContractError {


	INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT("A property definition lacks a default value and there are not sufficient property value assignments to cover the missing default value"),//
	NULL_PROPERTY_ID("Null property id"),
	NULL_PROPERTY_DEFINITION("Null property definition"),//
	NULL_PROPERTY_VALUE("Null property value"),
	DUPLICATE_PROPERTY_DEFINITION("Duplicate assignment of a property definition to a property id"),
	UNKNOWN_PROPERTY_ID("Unknown property id"),
	INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"),//
	DUPLICATE_PROPERTY_VALUE_ASSIGNMENT("Duplicate property value assignment"),
	NULL_PROPERTY_OWNER("Property value is being assigned to a null owner"),//
	
	
	NULL_PROPERTY_TYPE("Type for property definition is null"),//
	IMMUTABLE_VALUE("This property is defined as immutable"),//
	INCOMPATIBLE_DEFAULT_VALUE("Default value is incompatible with the class type"),//		
	PROPERTY_DEFINITION_IMPROPER_TYPE("Property definition has improper data type"),//
	
	
	NEGATIVE_INITIAL_SIZE("Negative initial size"),//
	NEGATIVE_CAPACITY_INCREMENT("Negative capacity increment"),//
	NEGATIVE_INDEX("Negative index"),//
	
	
	NULL_TIME_TRACKING_POLICY("Time tracking policy is null"),//
	TIME_TRACKING_OFF("Time tracking is off"),//
	

PROPERTY_DEFINITION_MISSING_DEFAULT("Property definition has no default value"),//	
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
