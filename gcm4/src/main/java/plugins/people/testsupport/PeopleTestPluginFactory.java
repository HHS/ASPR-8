package plugins.people.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain PeoplePlugin}. Provides
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
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Returns a list of plugins containing a People, Stochastic and Test Plugin
		 * built from the contributed PluginDatas.
		 * 
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link PeopleTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StocasticsPlugin is defaulted to one formed from
		 * {@link PeopleTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link PeopleTestPluginFactory#factory}
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
		 * Sets the {@link PeoplePluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *                           {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 * 
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			if (peoplePluginData == null) {
				throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
			}
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Sets the {@link StochasticsPluginData} in this Factory.
		 * This explicit instance of pluginData will be used to create a
		 * StochasticsPlugin
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
	 * needed to adequately test the {@link PeoplePlugin} by generating:
	 * <li>{@link PeoplePluginData},{@link StochasticsPluginData}
	 * <li>either directly (by default) via
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * <li>or explicitly set via
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * 
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
	 * needed to adequately test the {@link PeoplePlugin} by generating:
	 * <li>{@link PeoplePluginData},{@link StochasticsPluginData}
	 * <li>either directly (by default) via
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * <li>or explicitly set via
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * 
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
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the PeoplePlugin
	 * <li>The resulting PeoplePluginData will be empty;
	 * the equivalent of PeoplePluginData.builder().build()
	 */
	public static PeoplePluginData getStandardPeoplePluginData() {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate for
	 * testing the ResourcesPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
