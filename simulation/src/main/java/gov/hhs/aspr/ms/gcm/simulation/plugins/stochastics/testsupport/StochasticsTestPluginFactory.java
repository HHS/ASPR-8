package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain StochasticsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
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
	 * Factory class that facilitates the building of {@linkplain PluginData} with
	 * the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Returns a list of plugins containing a Stochastics and Test Plugin built from
		 * the contributed PluginDatas.
		 * <ul>
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link StochasticsTestPluginFactory#getStandardStochasticsPluginData}</li>
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link StochasticsTestPluginFactory#factory}</li>
		 * </ul>
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
		 * Sets the {@link StochasticsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a StochasticsPlugin
		 * 
		 * @throws ContractException {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
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
	 * needed to adequately test the {@link StochasticsPlugin} by generating:
	 * <ul>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA} if
	 *                           testPluginData is null
	 */
	public static Factory factory(long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(seed, testPluginData));

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link StochasticsPlugin} by generating:
	 * <ul>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractException {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
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
	 * testing the StochasticsPlugin. The resulting StocasticsPluginData will
	 * include:
	 * <ul>
	 * <li>Every randomGeneratorId included in {@link TestRandomGeneratorId}</li>
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		builder.setMainRNGState(wellState);
		for (TestRandomGeneratorId testRandomGeneratorId : TestRandomGeneratorId.values()) {
			wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
			builder.addRNG(testRandomGeneratorId, wellState);
		}
		return builder.build();
	}
}
