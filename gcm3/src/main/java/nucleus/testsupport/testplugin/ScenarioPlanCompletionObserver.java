package nucleus.testsupport.testplugin;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class ScenarioPlanCompletionObserver {
	private Boolean executed;
	
	public synchronized void handleOutput(Object output) {
		if(output instanceof TestScenarioReport) {
			if(executed!=null) {
				throw new UnsupportedOperationException("duplicate scenario reports");
			}
			TestScenarioReport testScenarioReport = (TestScenarioReport)output;
			executed = testScenarioReport.isComplete();
		}
	}
	
	public synchronized boolean allPlansExecuted() {
		if(executed == null) {
			return false;
		}
		return executed;
	}
	
	

}
