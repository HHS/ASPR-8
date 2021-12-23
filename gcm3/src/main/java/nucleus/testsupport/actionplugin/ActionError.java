package nucleus.testsupport.actionplugin;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum ActionError implements ContractError {

	ACTION_EXECUTION_FAILURE("Not all action plans were executed or no action plans were added to the action plugin");

	private final String description;

	private ActionError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
