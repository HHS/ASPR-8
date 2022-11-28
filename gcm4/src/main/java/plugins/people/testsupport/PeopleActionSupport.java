package plugins.people.testsupport;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import util.errors.ContractException;

/**
 * A static test support class for the globals plugin. Provides convenience
 * methods for integrating a test plugin into a people plugin simulation test
 * harness.
 * 
 * 
 * @author Shawn Hatch
 *
 */

public class PeopleActionSupport {

	private PeopleActionSupport() {
	}

	/**
	 * Creates the test plugin containing a test actor initialized by the given
	 * consumer. Executes the simulation via the
	 * {@linkplain PeopleActionSupport#testConsumers(Plugin)} method
	 *
	 */

	public static void testConsumer(Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData	.builder()//
														.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
														.build();

		Plugin plugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(plugin);
	}

	/**
	 * Executes a simulation composed of the given test plugin.
	 */
	public static void testConsumers(Plugin testPlugin) {

		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build());

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(peoplePlugin)//
					.addPlugin(testPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}
}
