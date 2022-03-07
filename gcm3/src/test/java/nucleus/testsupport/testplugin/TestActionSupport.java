package nucleus.testsupport.testplugin;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.util.ContractException;

public final class TestActionSupport {
	private TestActionSupport() {
	}

	public static void testConsumer(Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData	.builder()//
														.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
														.build();

		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		testConsumers(plugin);
	}

	public static void testConsumers(Plugin testPlugin) {

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
