package plugins.gcm.support;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum GcmError implements ContractError {

	COMPONENT_LACKS_PERMISSION("Current active component does not have permission");

	private final String description;

	private GcmError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
