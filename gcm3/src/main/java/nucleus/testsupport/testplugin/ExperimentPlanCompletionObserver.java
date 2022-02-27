package nucleus.testsupport.testplugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import nucleus.ExperimentContext;

@ThreadSafe
public final class ExperimentPlanCompletionObserver {
	
	private Map<Integer,TestScenarioReport> testScenarioReports = new LinkedHashMap<>(); 

	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(TestScenarioReport.class, this::handleActionCompletionReport);
	}

	private synchronized void handleActionCompletionReport(ExperimentContext experimentContext, Integer scenarioId, TestScenarioReport testScenarioReport) {
		testScenarioReports.put(scenarioId, testScenarioReport);
	}
	
	public synchronized Optional<TestScenarioReport> getActionCompletionReport(Integer scenarioId) {
		return Optional.ofNullable(testScenarioReports.get(scenarioId));
	}
	
	

}
