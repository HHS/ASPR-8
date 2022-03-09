package nucleus.testsupport.testplugin;

import net.jcip.annotations.ThreadSafe;

/**
 * A threadsafe output handler that handles all output from a simulation. A
 * single TestScenarioReport is produced at the end of of each scenario by the
 * TestPlanDataManager contributed by the TestPlugin. This utility expects
 * exactly one TestScenarioReport and will retain the success state of that
 * report. Used to determine if 1)the test plugin contained at least one plan
 * and 2) all such plans were executed.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class ScenarioPlanCompletionObserver {
	private Boolean executed;

	/**
	 * Handles all output from a simulation, but processes only
	 * TestScenarioReport items.
	 * 
	 * @throws UnsupportedOperationException
	 *             if multiple TestScenarioReport items are received
	 */
	public synchronized void handleOutput(Object output) {
		if (output instanceof TestScenarioReport) {
			if (executed != null) {
				throw new UnsupportedOperationException("duplicate scenario reports");
			}
			TestScenarioReport testScenarioReport = (TestScenarioReport) output;
			executed = testScenarioReport.isComplete();
		}
	}

	/**
	 * Returns true if and only if the TestScenarioReport was received and plans
	 * were completed.
	 */
	public synchronized boolean allPlansExecuted() {
		if (executed == null) {
			return false;
		}
		return executed;
	}

}
