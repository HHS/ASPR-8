package plugins.people.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the people plugin. Provides convenience
 * methods for obtaining standard People and Stochastics PluginData.
 * 
 * Also contains factory methods to obtain a list of plugins that can be
 * utilized with
 * {@code TestSimulation.executeSimulation()}
 * 
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
		 * Method that will get the PluginData for the People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a PeoplePlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the PeoplePluginData in this Factory.
		 * 
		 * @param peoplePluginData the PeoplePluginData you want to use, if different
		 *                         from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			this.data.peoplePluginData = peoplePluginData;
			return this;
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
	 * Method that will generate PeoplePluginData and StocasticsPluginData based on
	 * some configuration parameters.
	 * 
	 * @param seed     sued to seed a RandomGenerator
	 * @param consumer consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(long seed, Consumer<ActorContext> consumer) {
		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();

		return factory(seed, testPluginData);
	}

	/**
	 * Method that will generate PeoplePluginData and StocasticsPluginData based on
	 * some configuration parameters.
	 * 
	 * @param seed           used to seed a RandomGenerator
	 * @param testPluginData PluginData that will be used to generate a TestPlugin
	 * @return a new instance of factory
	 * 
	 */
	public static Factory factory(long seed, TestPluginData testPluginData) {
		return new Factory(new Data(seed, testPluginData));
	}

	/**
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * 
	 * @return the resulting PeoplePluginData
	 * 
	 */
	public static PeoplePluginData getStandardPeoplePluginData() {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		return peopleBuilder.build();
	}

	/**
	 * Method that will return a Standard StochasticsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param seed a seed to seed a RandomGenerator
	 * @return the resulting StocasticsPluginData
	 * 
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
	}

}
