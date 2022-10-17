package nucleus;

/**
 * The status of a scenario from the perspective of an experiment. If an
 * experiment is executed as a continuation of a previous execution, then the
 * scenarios fully recorded in the scenario progress file are marked as
 * PREVIOUSLY_SUCCEEDED. All other scenarios put into READY status. As the
 * experiment executes it updates the scenario status first to RUNNING until the
 * corresponding simulation completes. If the simulation runs without throwing
 * an exception, the scenario is updated to SUCCEDED. Otherwise, it is updated
 * to FAILED.
 * 
 * @author Shawn Hatch
 *
 */

public enum ScenarioStatus {
	READY, RUNNING, PREVIOUSLY_SUCCEEDED, SUCCEDED, FAILED;
}