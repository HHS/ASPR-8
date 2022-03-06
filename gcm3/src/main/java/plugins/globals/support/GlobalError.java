package plugins.globals.support;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum GlobalError implements ContractError {

	NULL_GLOBAL_COMPONENT_INITIAL_BEHVAVIOR_SUPPLIER("Null global component initial behvavior supplier"),
	
	NULL_GLOBAL_DATA_MANGER("Null global data manager"),
	
	NULL_GLOBAL_PLUGIN_DATA("Null global plugin data"),
	
	NULL_GLOBAL_PROPERTY_DEFINITION("Null global property definition"),
	
	NULL_GLOBAL_PROPERTY_ID("Null global property id"),
	
	NULL_GLOBAL_PROPERTY_VALUE("Null global property value"),
	
	UNKNOWN_GLOBAL_PROPERTY_ID("Unknown global property id"),
	
	INCOMPATIBLE_VALUE("Property value is incompatible with the global property definition"),
	
	DUPLICATE_GLOBAL_PROPERTY_DEFINITION("Duplicate global property definition"),
	
	DUPLICATE_GLOBAL_PROPERTY_VALUE_ASSIGNMENT("Duplicate global property value assignment"),
	
	INSUFFICIENT_GLOBAL_PROPERTY_VALUE_ASSIGNMENT("A global property definition default value is null and not replaced with sufficient property value assignments")
	;

	private final String description;

	private GlobalError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
