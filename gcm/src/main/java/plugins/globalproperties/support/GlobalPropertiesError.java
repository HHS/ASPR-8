package plugins.globalproperties.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum GlobalPropertiesError implements ContractError {


	NULL_GLOBAL_DATA_MANGER("Null global data manager"),
	NULL_GLOBAL_PLUGIN_DATA("Null global plugin data"),		
	NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA("Null global property report plugin data"),
	
	NULL_GLOBAL_PROPERTY_INITIALIZATION("Null global property initialization"),
	;

	private final String description;

	private GlobalPropertiesError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
