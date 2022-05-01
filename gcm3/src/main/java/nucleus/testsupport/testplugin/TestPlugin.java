package nucleus.testsupport.testplugin;

import java.util.List;
import java.util.Optional;

import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginContext;
import util.errors.ContractException;

/**
 * Static test support plugin that is designed to work with a unit testing
 * framework. It provides for the injection of behavior into actors and data
 * managers to test various simulation behaviors in a function/system setting.
 * 
 * @author Shawn Hatch
 *
 */
public class TestPlugin {

	private TestPlugin() {
	}

	/*
	 * Initializes a simulation via the given context. Using a TestPluginData
	 * retrieved from the context, this initializer adds test actor and test
	 * data manager instances that are used in testing. It also creates an
	 * TestPlanDataManager that is used internally to this plugin to help manage
	 * plan distribution for the aforementioned actors and data managers.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN_CONTEXT} if the
	 *             pluginContext is null</li>
	 */
	private static void init(PluginContext pluginContext) {
		if (pluginContext == null) {			
			throw new ContractException(NucleusError.NULL_PLUGIN_CONTEXT);
		}

		TestPluginData testPluginData = pluginContext.getPluginData(TestPluginData.class);

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

	public static Plugin getTestPlugin(TestPluginData testPluginData) {
		return Plugin	.builder()//
						.setInitializer(TestPlugin::init)//
						.addPluginData(testPluginData)//
						.setPluginId(TestPluginId.PLUGIN_ID)//
						.build();//
	}

}
