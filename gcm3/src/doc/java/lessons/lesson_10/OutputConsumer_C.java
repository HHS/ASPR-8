package lessons.lesson_10;

import nucleus.ExperimentContext;

public class SimpleOutputHandler {
	
	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(Object.class, this::handleOutput);
		
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToExperimentClose(this::handleExperimentClose);
		
		experimentContext.subscribeToSimulationOpen(this::handleSimulationOpen);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
		
	}

	private synchronized void handleOutput(ExperimentContext experimentContext, Integer scenarioId, Object output) {
		System.out.println("scenario " + scenarioId + ": " + output);
	}
	
	private synchronized void handleExperimentOpen(ExperimentContext experimentContext) {
		System.out.println("the experiment is open");
	}

	private synchronized void handleExperimentClose(ExperimentContext experimentContext) {
		System.out.println("the experiment is closed");
	}

	private synchronized void handleSimulationOpen(ExperimentContext experimentContext, Integer scenarioId) {
		System.out.println("scenario " + scenarioId + " is open");
	}

	private synchronized void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		System.out.println("scenario " + scenarioId + " is closed");
	}
	
	

	
}
