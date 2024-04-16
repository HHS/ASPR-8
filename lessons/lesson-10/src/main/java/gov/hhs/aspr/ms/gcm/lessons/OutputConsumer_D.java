package gov.hhs.aspr.ms.gcm.lessons;

import java.util.StringJoiner;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentContext;

/* start code_ref=output_consumer_D|code_cap=OutputConsumer_D demonstrates that the meta data collected from the dimensions is available from the experiment context. Thus output can be associated with the scenario's meta data.*/
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
		experimentContext.getScenarioMetaData(scenarioId).forEach(joiner::add);

		System.out.println(joiner);
	}

}
/* end */
