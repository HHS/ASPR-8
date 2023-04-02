package plugins.partitions.testsupport;

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
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.PartitionError;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.AttributesPluginId;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import util.errors.ContractException;

/**
 * A static test support class for the {@linkplain PartitionsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
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
			this.attributesPluginData = getStandardAttributesPluginData();
			this.partitionsPlugin = getStandardPartitionsPlugin();
			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
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
		 * Method that will get the PluginData for the Attributes, Partitions,
		 * People, Stochastic and Test Plugins and use the respective PluginData
		 * to build Plugins Returns a list of plugins containing a Attributes,
		 * Partitions, People, Stochastic and Test Plugin built from the
		 * contributed PluginDatas
		 * 
		 * <li>AttributesPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardAttributesPluginData}
		 * <li>PartitionsPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardPartitionsPlugin}
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link PartitionsTestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(this.data.attributesPluginData);

			Plugin partitionsPlugin = this.data.partitionsPlugin;

			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

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
		 * Sets the {@link AttributesPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a AttributesPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain AttributeError#NULL_ATTRIBUTES_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setAttributesPluginData(AttributesPluginData attributesPluginData) {
			if (attributesPluginData == null) {
				throw new ContractException(AttributeError.NULL_ATTRIBUTES_PLUGIN_DATA);
			}
			this.data.attributesPluginData = attributesPluginData;
			return this;
		}

		/**
		 * Sets the {@link PartitionsPlugin} in this Factory. This explicit
		 * instance of pluginData will be used to create a PartitionsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PartitionError#NULL_PARTITION_PLUGIN} if the
		 *             passed in pluginData is null
		 */
		public Factory setPartitionsPlugin(Plugin partitionsPlugin) {
			if (partitionsPlugin == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_PLUGIN);
			}
			this.data.partitionsPlugin = partitionsPlugin;
			return this;
		}

		/**
		 * Sets the {@link PeoplePluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if the
		 *             passed in pluginData is null
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
		 * @throws ContractExecption
		 *             {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
		 *             if the passed in pluginData is null
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
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link PartitionsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link AttributesPluginData}
	 * <li>{@link PartitionsPlugin}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardAttributesPluginData}
	 * <li>{@link #getStandardPartitionsPlugin}
	 * <li>{@link #getStandardPeoplePluginData}
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setAttributesPluginData}
	 * <li>{@link Factory#setPartitionsPlugin}
	 * <li>{@link Factory#setPeoplePluginData}
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_PLUGIN_DATA} if testPluginData
	 *             is null
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(initialPopulation, seed, testPluginData));
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link PartitionsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link AttributesPluginData}
	 * <li>{@link PartitionsPlugin}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardAttributesPluginData}
	 * <li>{@link #getStandardPartitionsPlugin}
	 * <li>{@link #getStandardPeoplePluginData}
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setAttributesPluginData}
	 * <li>{@link Factory#setPartitionsPlugin}
	 * <li>{@link Factory#setPeoplePluginData}
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if
	 *             consumer is null
	 */
	public static Factory factory(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginDataBuilder.build();

		return factory(initialPopulation, seed, testPluginData);
	}

	/**
	 * Returns a standardized AttributesPluginData that is minimally adequate
	 * for testing the PartitionsPlugin
	 * <li>The resulting AttributesPluginData will include:
	 * <ul>
	 * <li>Every AttributeId included in {@link TestAttributeId}
	 * <ul>
	 * <li>along with the attributeDefinition for each
	 * </ul>
	 * </ul>
	 */
	public static AttributesPluginData getStandardAttributesPluginData() {
		AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		return attributesBuilder.build();
	}

	/**
	 * Returns a Standardized PartitionsPlugin that is minimally adequate for
	 * testing the PartitionsPlugin
	 * <li>The resulting PartitionsPlugin will include:
	 * <ul>
	 * <li>the basic PartitionsPlugin from
	 * {@link PartitionsPlugin#getPartitionsPlugin}
	 * <li>An additional pluginDependency on {@link AttributesPluginId}
	 * </ul>
	 */
	public static Plugin getStandardPartitionsPlugin() {
		return PartitionsPlugin.getPartitionsPlugin(AttributesPluginId.PLUGIN_ID);
	}

	/**
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the PartitionsPlugin
	 * <li>The resulting PeoplePluginData will include:
	 * <ul>
	 * <li>a number of people equal to the passed in intialPopulation
	 * </ul>
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
	 * Returns a standardized StochasticsPluginData that is minimally adequate
	 * for testing the PartitionsPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		WellState wellState = WellState.builder().setSeed(seed).build();
		return StochasticsPluginData.builder().setMainRNG(wellState).build();
	}

}
