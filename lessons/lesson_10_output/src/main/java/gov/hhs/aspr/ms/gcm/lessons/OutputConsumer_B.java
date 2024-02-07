package gov.hhs.aspr.ms.gcm.lessons;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;

/* start code_ref=output_consumer_B|code_cap=Output consumer B has access to the experiment level data, so it prints the output to the console as before, but also adds the relevant scenario id.*/
public class OutputConsumer_B implements Consumer<ExperimentContext> {

	@Override
	public void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(Object.class, this::handleOutput);
	}

	private void handleOutput(ExperimentContext experimentContext, Integer scenarioId, Object output) {
		System.out.println("scenario " + scenarioId + ": " + output);
	}
}
/* end */
