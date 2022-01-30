package nucleus;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum ExperimentError implements ContractError {

	
	NULL_OUTPUT_ITEM("Null output"),
	NULL_EXPERIMENT("Null experiment"),
	NULL_EXPERIMENT_REPORT_PATH("Null experiment report path"),
	;

	private final String description;

	private ExperimentError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
