package plugins.util.properties;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum PropertyError implements ContractError {

	NULL_PROPERTY_TYPE("Type for property definition is null"),//
	IMMUTABLE_VALUE("This property is defined as immutable"),//
	INCOMPATIBLE_DEFAULT_VALUE("Default value is incompatible with the class type"),//
	INCOMPATIBLE_VALUE("Property value is incompatible with the property definition"),//
	NULL_PROPERTY_DEFINITION("Null property definition"),//
	PROPERTY_DEFINITION_IMPROPER_TYPE("Property definition has improper data type"),//
	PROPERTY_DEFINITION_MISSING_DEFAULT("Property definition has no default value"),//
	NEGATIVE_INITIAL_SIZE("Negative initial size"),//
	NEGATIVE_CAPACITY_INCREMENT("Negative capacity increment"),//
	NEGATIVE_INDEX("Negative index"),//
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
