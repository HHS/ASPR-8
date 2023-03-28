package nucleus;

/**
 * The status of a scenario from the perspective of an experiment. All scenarios
 * start as READY. If the experiment is executed as a continuation of a previous
 * execution, then the scenarios fully recorded in the scenario progress file
 * are marked as PREVIOUSLY_SUCCEEDED. If there are any explicitly added
 * scenarios, then only those scenarios can remain READY and all others are marked
 * SKIPPED. As the experiment executes the READY scenarios, it updates the
 * scenario status of each to RUNNING until the corresponding simulation
 * completes. If the simulation runs without throwing an exception, the scenario
 * is updated to SUCCEDED. Otherwise, it is updated to FAILED.
 * 
 *
 */

public enum ScenarioStatus {
	READY, SKIPPED, PREVIOUSLY_SUCCEEDED, RUNNING, SUCCEDED, FAILED;
}