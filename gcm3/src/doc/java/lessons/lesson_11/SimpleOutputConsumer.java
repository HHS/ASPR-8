package lessons.lesson_11;

import java.util.StringJoiner;
import java.util.function.Consumer;

import nucleus.ExperimentContext;

public class SimpleOutputConsumer implements Consumer<ExperimentContext> {

	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(Object.class, this::handleOutput);
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
	}

	private void handleOutput(ExperimentContext experimentContext, Integer scenarioId, Object output) {
		StringJoiner joiner = new StringJoiner("\t", "", "");
		
		joiner.add(" "+scenarioId.toString());
		experimentContext.getScenarioMetaData(scenarioId).get().forEach(joiner::add);
		joiner.add(output.toString());
		System.out.println(joiner);
	}

	private void handleExperimentOpen(ExperimentContext experimentContext) {
		StringJoiner joiner = new StringJoiner("\t", "", "");
		joiner.add(" scenario");
		experimentContext.getExperimentMetaData().forEach(joiner::add);
		joiner.add("output");
		System.out.println(joiner);
	}

}
