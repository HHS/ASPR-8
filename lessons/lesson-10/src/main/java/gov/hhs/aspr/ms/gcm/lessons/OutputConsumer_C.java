package gov.hhs.aspr.ms.gcm.lessons;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentContext;

/* start code_ref=output_consumer_C|code_cap=The output consumer C demonstrates the broader life cycle of the experiment context by printing out experiment and scenario status while still printing output to the console.*/
public class OutputConsumer_C implements Consumer<ExperimentContext> {

	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(Object.class, this::handleOutput);

		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToExperimentClose(this::handleExperimentClose);

		experimentContext.subscribeToSimulationOpen(this::handleSimulationOpen);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
	}

	private void handleOutput(ExperimentContext experimentContext, Integer scenarioId, Object output) {
		System.out.println("scenario " + scenarioId + ": " + output);
	}

	private void handleExperimentOpen(ExperimentContext experimentContext) {
		System.out.println("the experiment is open");
	}

	private void handleExperimentClose(ExperimentContext experimentContext) {
		System.out.println("the experiment is closed");
	}

	private void handleSimulationOpen(ExperimentContext experimentContext, Integer scenarioId) {
		System.out.println("scenario " + scenarioId + " is open");
	}

	private void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		System.out.println("scenario " + scenarioId + " is closed");
	}
}
/* end */
