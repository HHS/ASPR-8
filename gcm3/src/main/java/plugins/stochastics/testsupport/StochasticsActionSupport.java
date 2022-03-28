package plugins.stochastics.testsupport;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

/**
 * A static test support class for the stochastics plugin. Provides convenience
 * methods for integrating a test plugin into a stochastic simulation test
 * harness.
 * 
 * 
 * @author Shawn Hatch
 *
 */

public class StochasticsActionSupport {

	/**
	 * Creates the test plugin containing a test actor initialized by the given
	 * consumer. Executes the simulation via the
	 * {@linkplain StochasticsActionSupport#testConsumers(Plugin)} method
	 *
	 */
	public static void testConsumer(long seed, Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData	.builder()//
														.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
														.build();

		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(seed, plugin);
	}

	/**
	 * Executes a simulation composed of the given test plugin and the
	 * stochastics plugin initialized with the
	 * {@linkplain TestRandomGeneratorId} randdom generator ids and the given
	 * seed.
	 */
	public static void testConsumers(long seed, Plugin testPlugin) {

		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(seed);

		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(builder.build());
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPlugin(testPlugin)//
					.addPlugin(stochasticPlugin)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
