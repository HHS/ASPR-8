package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum TestError implements ContractError {
	DUPLICATE_TEST_SCENARIO_REPORTS("Duplicate test scenario reports"),
	MISSING_TEST_SCENARIO_REPORTS("Missing test scenario report likely due to not including a TestPlugin"),
	MULTIPLE_MATCHING_ITEMS("Multiple items were found matching "),
	NULL_ALIAS("Null alias value"),
	NULL_DATA_MANAGER_SUPPLIER("Null data manager supplier"), 
	NULL_OUTPUT_ITEM("Null released output item"), 
	NULL_PLAN("Null plan"),
	NULL_PLUGIN_ID("Null plugin id"),
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
