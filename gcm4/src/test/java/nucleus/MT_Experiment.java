package nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;

/**
 * Manual experiment test focusing on the generation and handling of exceptions.
 * 
 * @author Shawn Hatch
 *
 */
public class MT_Experiment {

	private static class ExperimentContextConsumer implements Consumer<ExperimentContext> {

		// this field is only safe to use in the main thread
		private ExperimentContext experimentContext;

		@Override
		public void accept(ExperimentContext experimentContext) {
			this.experimentContext = experimentContext;
			experimentContext.subscribeToExperimentClose(this::handleExperimentClose);
			experimentContext.subscribeToSimulationClose(this::handleSimulationClose);

		}

		private void handleSimulationClose(ExperimentContext experimentContext, Integer sceanrioId) {
			ScenarioStatus scenarioStatus = experimentContext.getScenarioStatus(sceanrioId).get();
			if (scenarioStatus == ScenarioStatus.FAILED) {
				System.out.println("SIMULATION CLOSE with scenario " + sceanrioId + " failing");
			}
		}

		private void handleExperimentClose(ExperimentContext experimentContext) {
			System.out.println();
			System.out.println("EXPERIMENT CLOSE");
			System.out.println();

			for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
				List<Integer> scenarios = experimentContext.getScenarios(scenarioStatus);

				System.out.println("There were " + scenarios.size() + " " + scenarioStatus + " scenarios");
			}
			System.out.println();
			List<Integer> failedScenarios = experimentContext.getScenarios(ScenarioStatus.FAILED);
			for (Integer scenarioId : failedScenarios) {
				Exception e = experimentContext.getScenarioFailureCause(scenarioId).get();
				System.out.println("Sceanrio " + scenarioId + " has failed with stackTrace");
				e.printStackTrace(System.out);
			}
		}

		/*
		 * Execute in the main thread only
		 */
		private void report() {
			System.out.println();
			System.out.println("FORCED STATUS UPDATE");
			System.out.println();

			for (ScenarioStatus scenarioStatus : ScenarioStatus.values()) {
				List<Integer> scenarios = experimentContext.getScenarios(scenarioStatus);

				System.out.println("There were " + scenarios.size() + " " + scenarioStatus + " scenarios");
			}
			System.out.println();
			List<Integer> failedScenarios = experimentContext.getScenarios(ScenarioStatus.FAILED);
			for (Integer scenarioId : failedScenarios) {
				Exception e = experimentContext.getScenarioFailureCause(scenarioId).get();
				System.out.println("Sceanrio " + scenarioId + " has failed with stackTrace");
				e.printStackTrace(System.out);
			}
		}

	}

	@Test
	public void test() {
		main(new String[] {});
	}

	private MT_Experiment() {

	}

	public static void main(String[] args) {
		new MT_Experiment().excecute();
	}

	private int counter;

	private final Object LOCK = new Object();

	private Dimension getDimension(final int dimSize) {
		Dimension.Builder builder = Dimension.builder();
		for (int i = 0; i < dimSize; i++) {
			builder.addLevel((c) -> new ArrayList<>());
		}
		return builder.build();
	}

	private void excecute() {

		// use the test plugin to generate an agent
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have one of the six actors throw an exception
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			synchronized (LOCK) {
				counter++;
				// System.out.println("counter = " + counter);
				if (counter == 3) {
					throw new RuntimeException("test exception");
				}
			}
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// build and execute the experiment

		Experiment	.builder()//
					.addPlugin(testPlugin)//
					.addDimension(getDimension(100))//
					.reportFailuresToConsole(true)//
					.reportProgressToConsole(true)//
					.setHaltOnException(false)//
					.setThreadCount(10)//
					.build()//
					.execute();//
	}

}
