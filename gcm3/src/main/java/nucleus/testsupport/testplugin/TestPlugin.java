package nucleus.testsupport.testplugin;

import java.util.List;
import java.util.Optional;

import nucleus.Plugin;
import nucleus.PluginContext;
import nucleus.PluginData;

/**
 * Test Support plugin that is designed to work with a unit testing framework.
 * It provides for 1)the injection of behavior into actors to test various
 * simulation behaviors in a function/system setting and 2) the collection of
 * observed events for comparison to expectations.
 * 
 * @author Shawn Hatch
 *
 */
public class TestPlugin {

	private TestPlugin() {
	}

	/*
	 * Initializes a simulation via the given context. Using an ActionPluginData
	 * retrieved from the context, this initializer adds ActionActor and
	 * ActionDataManager instances that are used in testing. It also creates an
	 * ActionPluginDataManager that is used internally to this plugin to help
	 * manage data for the aforementioned actors and data managers.
	 * 
	 * @throws RuntimeException <li>if the pluginContext is null</li>
	 */
	private static void init(PluginContext pluginContext) {
		if (pluginContext == null) {
			throw new RuntimeException("null plugin context");
		}

		TestPluginData testPluginData = pluginContext.getPluginData(TestPluginData.class).get();

		TestPlanDataManager testPlanDataManager = new TestPlanDataManager(testPluginData);
		pluginContext.addDataManager(testPlanDataManager);

		List<Object> dataManagerAliases = testPluginData.getTestDataManagerAliases();
		for (Object alias : dataManagerAliases) {
			Optional<TestDataManager> optional = testPluginData.getTestDataManager(alias);
			if (optional.isPresent()) {
				pluginContext.addDataManager(optional.get());
			}
		}

		for (Object alias : testPluginData.getTestActorAliases()) {
			pluginContext.addActor(new TestActor(alias)::init);
		}

	}

	public static Plugin getPlugin(PluginData pluginData) {
		return Plugin	.builder()//
						.setInitializer(TestPlugin::init)//
						.addPluginData(pluginData)//
						.setPluginId(TestPluginId.PLUGIN_ID)//
						.build();//
	}

}
