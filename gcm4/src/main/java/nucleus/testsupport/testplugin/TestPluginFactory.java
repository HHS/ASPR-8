package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;

/**
 * A static test support class for the test plugin.
 * 
 * contains factory methods to obtain a list of plugins that can be
 * utilized with
 * {@code TestSimulation.executeSimulation()}
 * 
 */
public final class TestPluginFactory {
	private TestPluginFactory() {
	}

	private static class Data {
		private TestPluginData testPluginData;

		private Data(TestPluginData testPluginData) {
			this.testPluginData = testPluginData;
		}
	}

	/**
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Method that will get the PluginData for the
		 * Test Plugin
		 * and use the respective PluginData to build the Plugin
		 * 
		 * @return a List containing
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}
	}

	/**
	 * Method that will generate TestPluginData based on a consumer
	 * 
	 * @param consumer consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();

		return factory(testPluginData);
	}

	/**
	 * Method that will generate TestPluginData based on passed in TestPluginData
	 * 
	 * @param testPluginData PluginData that will be used to generate a TestPlugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(TestPluginData testPluginData) {
		return new Factory(new Data(testPluginData));
	}
}
