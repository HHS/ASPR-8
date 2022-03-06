package plugins.globals.testsupport;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.globals.GlobalPlugin;
import plugins.globals.GlobalPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import util.ContractException;

public class GlobalsActionSupport {

	public static void testConsumer(Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData	.builder()//
														.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
														.build();

		Plugin plugin = TestPlugin.getPlugin(testPluginData);
		testConsumers(plugin);
	}

	public static void testConsumers(Plugin testPlugin) {

		GlobalPluginData.Builder globalsPluginBuilder = GlobalPluginData.builder();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId, testGlobalPropertyId.getPropertyDefinition());
		}
		GlobalPluginData globalPluginData = globalsPluginBuilder.build();
		Plugin globalsPlugin = GlobalPlugin.getPlugin(globalPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()))//
					.addPlugin(globalsPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
