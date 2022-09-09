package lessons.lesson_10;

import java.util.StringJoiner;

import nucleus.ExperimentContext;

public class MetaDataOutputHandler {
	
	public synchronized void init(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToSimulationOpen(this::handleSimulationOpen);
	}

	
	private synchronized void handleExperimentOpen(ExperimentContext experimentContext) {
		
		
		StringJoiner joiner = new StringJoiner("\t", "", "");
		joiner.add("scenario");
		experimentContext.getExperimentMetaData().forEach(joiner::add);
		
		System.out.println(joiner);
	}


	private synchronized void handleSimulationOpen(ExperimentContext experimentContext, Integer scenarioId) {
		
		StringJoiner joiner = new StringJoiner("\t", "", "");
		joiner.add(scenarioId.toString());
		experimentContext.getScenarioMetaData(scenarioId).get().forEach(joiner::add);
		
		System.out.println(joiner);
	}
	
}
