package plugins.stochastics.testsupport;

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
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

/**
 * A static test support class for the {@linkplain StochasticsPlugin}. Provides
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
public class StochasticsTestPluginFactory {

	private StochasticsTestPluginFactory() {
	}

	private static class Data {
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(long seed, TestPluginData testPluginData) {
			this.stochasticsPluginData = getStandardStochasticsPluginData(seed);
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
		 * Method that will get the currently set PluginData for the Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the StochasticsPluginData in this Factory.
		 * 
		 * @param stochasticsPluginData the StochasticsPluginData you want to use, if
		 *                              different
		 *                              from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link StocasticsPlugin} by generating:
	 * <p>
	 * {@link StochasticsPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @param seed     Used to seed the StocasticsPluginData
	 * @param consumer used to generate TestPluginData
	 * @return a instance of Factory
	 * 
	 */
	public static Factory factory(long seed, Consumer<ActorContext> consumer) {

		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();

		return factory(seed, testPluginData);
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link StocasticsPlugin} by generating:
	 * <p>
	 * {@link StochasticsPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @param seed           Used to seed the StocasticsPluginData
	 * @param testPluginData Used to generate a TestPlugin
	 * @return a instance of Factory
	 * 
	 */
	public static Factory factory(long seed, TestPluginData testPluginData) {
		return new Factory(new Data(seed, testPluginData));

	}

	/**
	 * Creates a Standarized StocasticsPluginData that is minimally adequate for
	 * testing the StocasticsPlugin.
	 * <p>
	 * The resulting StocasticsPluginData will include:
	 * <li>Every randomGeneratorId included in {@link TestRandomGeneratorId}</li>
	 * </p>
	 * 
	 * @param seed used to seed the StocasticsPluginData
	 * @return the Standarized StocasticsPluginData
	 * 
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			builder.addRandomGeneratorId(testRandomGeneratorId);
		}
		builder.setSeed(seed);

		return builder.build();
	}
}
