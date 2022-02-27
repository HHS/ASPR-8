package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.PluginInitializer;

/**
 * Test Support plugin that is designed to work with a unit testing framework.
 * It provides for 1)the injection of behavior into actors to test various
 * simulation behaviors in a function/system setting and 2) the collection of
 * observed events for comparison to expectations.
 * 
 * @author Shawn Hatch
 *
 */
public class TestPluginInitializer implements PluginInitializer {

	

	/**
	 * Initializes a simulation via the given context. Using an ActionPluginData
	 * retrieved from the context, this initializer adds ActionActor and
	 * ActionDataManager instances that are used in testing. It also creates an
	 * ActionPluginDataManager that is used internally to this plugin to help
	 * manage data for the aforementioned actors and data managers.
	 * 
	 * @throws RuntimeException
	 *             <li>if the pluginContext is null</li>
	 */
	public void init(PluginContext pluginContext) {
		if (pluginContext == null) {
			throw new RuntimeException("null plugin context");
		}

		TestPluginData testPluginData = pluginContext.getPluginData(TestPluginData.class).get();

		TestPluginDataManager testPluginDataManager = new TestPluginDataManager(testPluginData);
		pluginContext.addDataManager(testPluginDataManager);

		List<Object> dataManagerAliases = testPluginData.getTestDataManagerAliases();		
		for (Object alias : dataManagerAliases) {
			Class<? extends TestDataManager> c = testPluginData.getTestDataManagerType(alias).get();
			TestDataManager testDataManager;
			try {
				testDataManager = c.newInstance();
				testDataManager.setAlias(alias);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			pluginContext.addDataManager(testDataManager);
		}

		for (Object alias : testPluginData.getTestActorAliases()) {
			pluginContext.addActor(new TestActor(alias)::init);
		}

	}


	@Override
	public PluginId getPluginId() {
		return TestPluginId.PLUGIN_ID;
	}

}
