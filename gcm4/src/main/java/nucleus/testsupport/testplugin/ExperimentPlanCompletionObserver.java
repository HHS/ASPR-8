package nucleus.testsupport.testplugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import nucleus.ExperimentContext;

/**
 * A threadsafe output handler that subscribes to TestScenarioReport. A single
 * TestScenarioReport is produced at the end of of each scenario by the
 * TestPlanDataManager contributed by the TestPlugin. Each report is kept and
 * can be accessed by scenario id.
 * 
 *
 */

@ThreadSafe
public final class ExperimentPlanCompletionObserver {

	private Map<Integer, TestScenarioReport> testScenarioReports = new LinkedHashMap<>();

	/**
	 * Initializes this observer by subscribing to TestScenarioReport
	 */
	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(TestScenarioReport.class, this::handleActionCompletionReport);
	}

	private synchronized void handleActionCompletionReport(ExperimentContext experimentContext, Integer scenarioId, TestScenarioReport testScenarioReport) {
		testScenarioReports.put(scenarioId, testScenarioReport);
	}

	/**
	 * Returns the TestScenarioReport collected for the given scenario id.
	 */
	public synchronized Optional<TestScenarioReport> getActionCompletionReport(Integer scenarioId) {
		return Optional.ofNullable(testScenarioReports.get(scenarioId));
	}

}
