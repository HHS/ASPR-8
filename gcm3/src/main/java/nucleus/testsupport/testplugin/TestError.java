package nucleus.testsupport.testplugin;

import nucleus.util.ContractError;
import nucleus.util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum TestError implements ContractError {

	TEST_EXECUTION_FAILURE("Not all action plans were executed or no action plans were added to the action plugin"),
	UNKNOWN_DATA_MANAGER_ALIAS("A data manager test plan was submitted under an alias that does not have a test data manager class type"),
	
	;

	private final String description;

	private TestError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
