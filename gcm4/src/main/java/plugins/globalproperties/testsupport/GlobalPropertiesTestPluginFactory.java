package plugins.globalproperties.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;

/**
 * A static test support class for the {@linkplain GlobalPropertiesPlugin}.
 * Provides
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
public final class GlobalPropertiesTestPluginFactory {

	private GlobalPropertiesTestPluginFactory() {
	}

	private static class Data {
		private GlobalPropertiesPluginData globalPropertiesPluginData;
		private TestPluginData testPluginData;

		private Data(TestPluginData testPluginData) {
			this.globalPropertiesPluginData = getStandardGlobalPropertiesPluginData();
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
		 * Method that will get the PluginData for the GlobalProperties and TestPlugin
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a GlobalPropertiesPlugin and a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();
			Plugin globalPropertiesPlugin = GlobalPropertiesPlugin
					.getGlobalPropertiesPlugin(this.data.globalPropertiesPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(globalPropertiesPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the GlobalPropertiesPluginData in this Factory.
		 * 
		 * @param globalPropertiesPluginData the GlobalPropertiesPluginData you want to
		 *                                   use, if different from the standard
		 *                                   PluginData
		 * 
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setGlobalPropertiesPluginData(GlobalPropertiesPluginData globalPropertiesPluginData) {
			this.data.globalPropertiesPluginData = globalPropertiesPluginData;
			return this;
		}
	}

	/**
	 * Method that will generate GlobalPropertiesPluginData
	 * 
	 * @param testPluginData PluginData that will be used to generate a TestPlugin
	 * 
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(TestPluginData testPluginData) {
		return new Factory(new Data(testPluginData));
	}

	/**
	 * Method that will generate GlobalPropertiesPluginData and TestPluginData
	 * 
	 * @param consumer consumer used to generate TestPluginData
	 * 
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(testPluginData);
	}

	/**
	 * Method that will return a Standard GlobalPropertiesPluginData
	 * 
	 * @return the resulting GlobalPropertiesPluginData
	 * 
	 */
	public static GlobalPropertiesPluginData getStandardGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId,
					testGlobalPropertyId.getPropertyDefinition());
		}

		return globalsPluginBuilder.build();
	}

}
