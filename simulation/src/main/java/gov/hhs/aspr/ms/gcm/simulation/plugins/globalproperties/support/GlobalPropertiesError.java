package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum GlobalPropertiesError implements ContractError {

 
	NULL_GLOBAL_PLUGIN_DATA("Null global plugin data"),//
	NULL_GLOBAL_PROPERTY_INITIALIZATION("Null global property initialization"),//
	NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA("Null global property report plugin data"),//
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
