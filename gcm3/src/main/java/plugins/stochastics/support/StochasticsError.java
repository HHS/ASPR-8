package plugins.stochastics.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum StochasticsError implements ContractError {
	NULL_SEED("Null seed value"),//
	NULL_RANDOM_NUMBER_GENERATOR("Null random number generator"),//
	DUPLICATE_RANDOM_NUMBER_GENERATOR_ID("Duplicate random number generator id"),//
	NULL_RANDOM_NUMBER_GENERATOR_ID("Null random number generator id"),//
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
