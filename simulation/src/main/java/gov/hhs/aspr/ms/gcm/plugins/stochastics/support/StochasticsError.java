package gov.hhs.aspr.ms.gcm.plugins.stochastics.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum StochasticsError implements ContractError {
	ILLEGAL_SEED_ININITIAL_STATE("Illegal seed initial state"),
	NULL_RANDOM_NUMBER_GENERATOR_ID("Null random number generator id"), //
	NULL_SEED("Null seed value"), //
	NULL_STOCHASTICS_PLUGIN_DATA("null stochastics plugin data"),
	NULL_WELL_STATE("Null well state"), //
	RANDOM_NUMBER_GENERATOR_ID_ALREADY_EXISTS("The random number generator id is already known"), //
	UNKNOWN_RANDOM_NUMBER_GENERATOR_ID("Unknown random number generator id"), //
	;

	private final String description;

	private StochasticsError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
