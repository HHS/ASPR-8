package gov.hhs.aspr.ms.gcm.lessons;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;

/* start code_ref=output_consumer_B */
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
