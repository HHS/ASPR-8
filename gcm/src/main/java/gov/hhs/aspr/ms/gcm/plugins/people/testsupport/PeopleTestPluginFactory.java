package gov.hhs.aspr.ms.gcm.plugins.people.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.errors.ContractException;

/**
 * A static test support class for the {@linkplain PeoplePlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
 */
public class PeopleTestPluginFactory {

	private PeopleTestPluginFactory() {
	}

	private static class Data {
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(long seed, TestPluginData testPluginData) {

			this.peoplePluginData = getStandardPeoplePluginData();
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
		 * Returns a list of plugins containing a People, Stochastic and Test Plugin
		 * built from the contributed PluginDatas.
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link PeopleTestPluginFactory#getStandardPeoplePluginData}</li>
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link PeopleTestPluginFactory#getStandardStochasticsPluginData}</li>
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link PeopleTestPluginFactory#factory}</li>
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Sets the {@link PeoplePluginData} in this Factory. This explicit instance of
		 * pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if
		 *                           the passed in pluginData is null
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			if (peoplePluginData == null) {
				throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
			}
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Sets the {@link StochasticsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a StochasticsPlugin
		 * 
		 * @throws ContractExecption {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
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
	 * needed to adequately test the {@link PeoplePlugin} by generating:
	 * <ul>
	 * <li>{@link PeoplePluginData}</li>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption {@linkplain NucleusError#NULL_PLUGIN_DATA} if
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
	 * needed to adequately test the {@link PeoplePlugin} by generating:
	 * <ul>
	 * <li>{@link PeoplePluginData}</li>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
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
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the PeoplePlugin
	 * The resulting PeoplePluginData will be empty</li>
	 * <ul>
	 * <li>the equivalent of PeoplePluginData.builder().build()
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData() {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate for
	 * testing the PeoplePlugin
	 * The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the passed
	 * in seed</li></li>
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		WellState wellState = WellState.builder().setSeed(seed).build();
		return StochasticsPluginData.builder().setMainRNGState(wellState).build();
	}

}
