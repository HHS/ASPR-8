package lessons.lesson_10;

import nucleus.ExperimentContext;

public class VerySimpleOutputHandler {
	
	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(Object.class, this::handleOutput);
	}

	private synchronized void handleOutput(ExperimentContext experimentContext, Integer scenarioId, Object output) {
		System.out.println("scenario " + scenarioId + ": " + output);
	}
}
