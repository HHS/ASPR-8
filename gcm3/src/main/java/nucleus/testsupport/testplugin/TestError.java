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
	NULL_ALIAS("Null alias value"),
	NULL_DATA_MANAGER_SUPPLIER("Null data manager supplier"),
	NEGATIVE_PLANNING_TIME("Negative test planning time"),
	NULL_PLAN("Null plan"),
	TEST_EXECUTION_FAILURE("Not all action plans were executed or no action plans were added to the action plugin"),
	UNKNOWN_DATA_MANAGER_ALIAS("A data manager test plan was submitted under an alias that does not have a test data manager class type"),
	
	;

	private final String description;

	private TestError(final String description) {
		this.description = description;
	}

	/**
	 * Returns the text description of the error
	 */
	@Override
	public String getDescription() {
		return description;
	}
}
