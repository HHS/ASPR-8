package lesson;

import java.util.StringJoiner;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;

/* start code_ref=output_consumer_D */
public class OutputConsumer_D implements Consumer<ExperimentContext> {

	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToSimulationOpen(this::handleSimulationOpen);
	}

	private void handleExperimentOpen(ExperimentContext experimentContext) {

		StringJoiner joiner = new StringJoiner("\t", "", "");
		joiner.add("scenario");
		experimentContext.getExperimentMetaData().forEach(joiner::add);

		System.out.println(joiner);
	}

	private void handleSimulationOpen(ExperimentContext experimentContext, Integer scenarioId) {

		StringJoiner joiner = new StringJoiner("\t", "", "");
		joiner.add(scenarioId.toString());
		experimentContext.getScenarioMetaData(scenarioId).get().forEach(joiner::add);

		System.out.println(joiner);
	}

}
/* end */
