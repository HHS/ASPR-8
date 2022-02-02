package plugins.stochastics.testsupport;

import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;

public class StochasticsActionSupport {
	
	public static void testConsumer(long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(seed, pluginBuilder.build());
	}

	
	public static void testConsumers(long seed, ActionPlugin actionPlugin) {
		Builder simBuilder = Simulation.builder();

		// add the stochastics plugin
		StochasticsPlugin.Builder stochasticsBuilder = StochasticsPlugin.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			stochasticsBuilder.addRandomGeneratorId(testRandomGeneratorId);
		}
		stochasticsBuilder.setSeed(seed);

		simBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, stochasticsBuilder.build()::init);
		// add the action plugin
		simBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		simBuilder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}
	}
}
