package plugins.gcm.input;

/**
 * A {@link RuntimeException} thrown during Scenario/Experiment construction. It
 * indicates that the cause of the error is very likely due to invalid input and
 * not an underlying error in GCM. It contains a ScenarioErrorType that
 * indicates the general cause of the exception and may also contain additional
 * message text.
 * 
 * Although this is a RuntimeException, it functions like a checked exception,
 * leaving the object throwing the exception in a recoverable state.
 * 
 * @author Shawn Hatch
 *
 */
public class ScenarioException extends RuntimeException {

	private static final long serialVersionUID = 8956278699714119176L;

	private final ScenarioErrorType scenarioErrorType;

	public ScenarioException(ScenarioErrorType scenarioErrorType) {
		super(scenarioErrorType.getDescription());
		this.scenarioErrorType = scenarioErrorType;
	}

	public ScenarioException(ScenarioErrorType scenarioErrorType, String description) {
		super(scenarioErrorType.getDescription() + ":" + description);
		this.scenarioErrorType = scenarioErrorType;
	}

	/**
	 * Returns the ScenarioErrorType that documents the general issue that
	 * caused the exception.
	 */
	public ScenarioErrorType getScenarioErrorType() {
		return scenarioErrorType;
	}
}