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
import plugins.globalproperties.support.GlobalPropertiesError;
import util.errors.ContractException;

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
 * <li>{@link TestSimulation#executeSimulation}
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
		 * Method that will get the currently set PluginData for the GlobalProperties
		 * and TestPlugins
		 * and use the respective PluginData to build the Plugins
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
		 * @throws ContractExecption
		 *                           {@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 * 
		 */

		public Factory setGlobalPropertiesPluginData(GlobalPropertiesPluginData globalPropertiesPluginData) {
			if (globalPropertiesPluginData == null) {
				throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA);
			}
			this.data.globalPropertiesPluginData = globalPropertiesPluginData;
			return this;
		}
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link GlobalPropertiesPlugin} by generating:
	 * <p>
	 * {@link GlobalPropertiesPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardGlobalPropertiesPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setGlobalPropertiesPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
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
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link GlobalPropertiesPlugin} by generating:
	 * <p>
	 * {@link GlobalPropertiesPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardGlobalPropertiesPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setGlobalPropertiesPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
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
	 * Creates a Standardized GlobalPropertiesPluginData that is minimally adequate
	 * for testing the GlobalPropertiesPlugin
	 * <p>
	 * The resulting GlobalPropertiesPluginData will include:
	 * <li>Every globalPropertyId included in {@link TestGlobalPropertyId} along
	 * with the defined propertyDefinition for each
	 * 
	 * @return the Standardized GlobalPropertiesPluginData
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
