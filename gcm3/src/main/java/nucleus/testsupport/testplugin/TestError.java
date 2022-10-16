package nucleus.testsupport.testplugin;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum TestError implements ContractError {
	DUPLICATE_TEST_SCENARIO_REPORTS("Duplicate test scenario reports"),
	NULL_ALIAS("Null alias value"),
	NULL_PLUGIN_ID("Null plugin id"),
	NULL_DATA_MANAGER_SUPPLIER("Null data manager supplier"),
	NEGATIVE_PLANNING_TIME("Negative test planning time"),
	NULL_PLAN("Null plan"),
	TEST_EXECUTION_FAILURE("Not all action plans were executed or no action plans were added to the test plugin"),
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
