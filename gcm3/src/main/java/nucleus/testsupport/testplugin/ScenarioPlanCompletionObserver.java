package nucleus.testsupport.testplugin;

public final class ScenarioPlanCompletionObserver {
	private Boolean executed;
	public void handleOutput(Object output) {
		if(output instanceof TestScenarioReport) {
			if(executed!=null) {
				throw new UnsupportedOperationException("duplicate scenario reports");
			}
			TestScenarioReport testScenarioReport = (TestScenarioReport)output;
			executed = testScenarioReport.isComplete();
		}
	}
	
	public boolean allPlansExecuted() {
		if(executed == null) {
			return false;
		}
		return executed;
	}
	
	

}
