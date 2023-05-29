package nucleus.testsupport.runcontinuityplugin;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum RunContinuityError implements ContractError {
	
	NULL_RUN_CONTINUITY_PLUGN_DATA("Null run continuity plugin data"), //
	;

	private final String description;

	private RunContinuityError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
