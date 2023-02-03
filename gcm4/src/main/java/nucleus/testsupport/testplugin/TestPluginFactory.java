package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;

/**
 * A static test support class for the {@linkplain TestPlugin}. Provides
 * convenience
 * methods for obtaining standarized PluginData for the listed Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be
 * utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation(List)}</li>
 * <li>or
 * <li>{@link TestSimulation#executeSimulation(List, nucleus.testsupport.testplugin.TestSimulationOutputConsumer)}
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
