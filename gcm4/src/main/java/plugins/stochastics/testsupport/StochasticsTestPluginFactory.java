package plugins.stochastics.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import util.errors.ContractException;

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
		 * Returns a list of plugins containing a Stochastics and Test Plugin built from
		 * the contributed PluginDatas.
		 * 
		 * <li>StocasticsPlugin is defaulted to one formed from
		 * {@link StochasticsTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link StochasticsTestPluginFactory#factory}
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
		 * Sets the {@link StochasticsPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * GroupsPlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 * 
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			if (stochasticsPluginData == null) {
				throw new ContractException(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA);
			}
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link StocasticsPlugin} by generating:
	 * <li>{@link StochasticsPluginData}
	 * 
	 * <li>either directly (by default) via
	 * {@link #getStandardStochasticsPluginData}
	 * <li>or explicitly set via
	 * {@link Factory#setStochasticsPluginData}
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_PLUGIN_DATA}
	 *                           if testPluginData is null
	 * 
	 */
	public static Factory factory(long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(seed, testPluginData));

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link StocasticsPlugin} by generating:
	 * <li>{@link StochasticsPluginData}
	 * 
	 * <li>either directly (by default) via
	 * {@link #getStandardStochasticsPluginData}
	 * <li>or explicitly set via
	 * {@link Factory#setStochasticsPluginData}
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
	 *                           if consumer is null
	 */
	public static Factory factory(long seed, Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();

		return factory(seed, testPluginData);
	}

	/**
	 * Creates a Standardized StocasticsPluginData that is minimally adequate for
	 * testing the StocasticsPlugin.
	 * <li>The resulting StocasticsPluginData will include:
	 * <li>Every randomGeneratorId included in {@link TestRandomGeneratorId}</li>
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
