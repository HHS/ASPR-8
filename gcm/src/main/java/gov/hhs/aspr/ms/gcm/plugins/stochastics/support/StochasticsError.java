package gov.hhs.aspr.ms.gcm.plugins.stochastics.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum StochasticsError implements ContractError {
	NULL_SEED("Null seed value"),//
	NULL_RANDOM_NUMBER_GENERATOR("Null random number generator"),//
	NULL_RANDOM_NUMBER_GENERATOR_ID("Null random number generator id"),//
	UNKNOWN_RANDOM_NUMBER_GENERATOR_ID("Unknown random number generator id"),//
	RANDOM_NUMBER_GENERATOR_ID_ALREADY_EXISTS("The random number generator id is already known"),//
	NULL_WELL_STATE("Null well state"),//
	NULL_STOCHASTICS_PLUGIN_DATA("null stochastics plugin data"),
	ILLEGAL_SEED_ININITIAL_STATE("Illegal seed initial state"),
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
