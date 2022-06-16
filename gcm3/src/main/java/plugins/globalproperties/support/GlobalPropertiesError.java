package plugins.globalproperties.support;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum GlobalPropertiesError implements ContractError {

	NULL_GLOBAL_COMPONENT_INITIAL_BEHVAVIOR_SUPPLIER("Null global component initial behvavior supplier"),
	NULL_GLOBAL_DATA_MANGER("Null global data manager"),
	NULL_GLOBAL_PLUGIN_DATA("Null global plugin data"),
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
