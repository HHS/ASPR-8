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
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.ContractException;

public class StochasticsActionSupport {
	
	public static void testConsumer(long seed, Consumer<ActorContext> consumer) {
		
		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();
		
		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		testConsumers(seed, plugin);
	}

	
	public static void testConsumers(long seed, Plugin testPlugin) {
		
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(seed);
		
		Plugin stochasticPlugin = StochasticsPlugin.getPlugin(builder.build());
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation.builder()//
		.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)
		.addPlugin(testPlugin)//
		.addPlugin(stochasticPlugin)//
		.build()//
		.execute();//
		
		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
