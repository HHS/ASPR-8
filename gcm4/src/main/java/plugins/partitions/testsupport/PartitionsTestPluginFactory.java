package plugins.partitions.testsupport;

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
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.AttributesPluginId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the partitions plugin. Provides convenience
 * methods for obtaining standard Attributes, People and
 * Stochastics PluginData.
 * 
 * Also contains factory methods to obtain a list of plugins that can be
 * utilized with
 * {@code TestSimulation.executeSimulation()}
 * 
 */
public class PartitionsTestPluginFactory {

	private PartitionsTestPluginFactory() {
	}

	private static class Data {
		private AttributesPluginData attributesPluginData;
		private Plugin partitionsPlugin;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, long seed, TestPluginData testPluginData) {
			this.attributesPluginData = PartitionsTestPluginFactory.getStandardAttributesPluginData();
			this.partitionsPlugin = PartitionsTestPluginFactory.getStandardPartitionsPlugin();
			this.peoplePluginData = PartitionsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);
			this.stochasticsPluginData = PartitionsTestPluginFactory.getStandardStochasticsPluginData(seed);
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
		 * Method that will get the PluginData for the Attributes, Partitions, People,
		 * Stochastic and Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a AttributesPlugin, PartitionsPlugin, PeoplePlugin,
		 *         StochasticsPlugin and a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(this.data.attributesPluginData);

			Plugin partitionsPlugin = this.data.partitionsPlugin;

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(attributesPlugin);
			pluginsToAdd.add(partitionsPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the AttributesPluginData in this Factory.
		 * 
		 * @param attributesPluginData the AttributesPluginData you want to use, if
		 *                             different
		 *                             from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setAttributesPluginData(AttributesPluginData attributesPluginData) {
			this.data.attributesPluginData = attributesPluginData;
			return this;
		}

		/**
		 * Method to set the PartitionsPlugin in this Factory.
		 * 
		 * @param partitionsPlugin the PartitionsPluginData you want to use, if
		 *                         different
		 *                         from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setPartitionsPlugin(Plugin partitionsPlugin) {
			this.data.partitionsPlugin = partitionsPlugin;
			return this;
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
	 * Method that will generate AttributesPluginData, PartitionsPlugin,
	 * PeoplePluginData, and StocasticsPluginData based on some configuration
	 * parameters
	 * 
	 * @param initialPopulation the initial population of the simulation
	 * @param seed              used to seed a RandomGenerator
	 * @param consumer          consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginDataBuilder.build();

		return factory(initialPopulation, seed, testPluginData);
	}

	/**
	 * Method that will generate AttributesPluginData, PartitionsPlugin,
	 * PeoplePluginData, and StocasticsPluginData based on some configuration
	 * parameters
	 * 
	 * @param initialPopulation the initial population of the simulation
	 * @param seed              used to seed a RandomGenerator
	 * @param testPluginData    PluginData that will be used to generate a
	 *                          TestPlugin
	 * @return a new instance of Facotry
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {

		return new Factory(new Data(initialPopulation, seed, testPluginData));
	}

	/**
	 * Method that will return a Standard AttributesPluginData based on some
	 * confirguration paramters
	 * 
	 * @return the resulting AttributesPluginData
	 * 
	 */
	public static AttributesPluginData getStandardAttributesPluginData() {
		AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		return attributesBuilder.build();
	}

	/**
	 * Method that will return a Standard PartitionsPlugin that additionally depends
	 * on the AttributesPlugin
	 * 
	 * @return the resulting PartitionsPlugin
	 * 
	 */
	public static Plugin getStandardPartitionsPlugin() {
		return PartitionsPlugin.getPartitionsPlugin(AttributesPluginId.PLUGIN_ID);
	}

	/**
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * @return the resulting PeoplePluginData
	 * 
	 */
	/**
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation the initial population of the simulation
	 * @return the resulting PeoplePluginData
	 * 
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}

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