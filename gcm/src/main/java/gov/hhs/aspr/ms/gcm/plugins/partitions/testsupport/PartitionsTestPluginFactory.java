package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport;

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
import gov.hhs.aspr.ms.gcm.plugins.partitions.PartitionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.AttributesPluginId;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.AttributeError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.errors.ContractException;

/**
 * A static test support class for the {@linkplain PartitionsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * {@link TestSimulation#execute}
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
	 * Factory class that facilitates the building of {@linkplain PluginData} with
	 * the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Method that will get the PluginData for the Attributes, Partitions, People,
		 * Stochastic and Test Plugins and use the respective PluginData to build
		 * Plugins Returns a list of plugins containing a Attributes, Partitions,
		 * People, Stochastic and Test Plugin built from the contributed PluginDatas
		 * <li>AttributesPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardAttributesPluginData}</li>
		 * <li>PartitionsPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardPartitionsPlugin}</li>
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardPeoplePluginData}</li>
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link PartitionsTestPluginFactory#getStandardStochasticsPluginData}</li>
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link PartitionsTestPluginFactory#factory}</li>
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
		 * Sets the {@link AttributesPluginData} in this Factory. This explicit instance
		 * of pluginData will be used to create a AttributesPlugin
		 * 
		 * @throws ContractExecption {@linkplain AttributeError#NULL_ATTRIBUTES_PLUGIN_DATA}
		 *                           if the passed in pluginData is null
		 */
		public Factory setAttributesPluginData(AttributesPluginData attributesPluginData) {
			if (attributesPluginData == null) {
				throw new ContractException(AttributeError.NULL_ATTRIBUTES_PLUGIN_DATA);
			}
			this.data.attributesPluginData = attributesPluginData;
			return this;
		}

		/**
		 * Sets the {@link PartitionsPlugin} in this Factory. This explicit instance of
		 * pluginData will be used to create a PartitionsPlugin
		 * 
		 * @throws ContractExecption {@linkplain PartitionError#NULL_PARTITION_PLUGIN}
		 *                           if the passed in pluginData is null
		 */
		public Factory setPartitionsPlugin(Plugin partitionsPlugin) {
			if (partitionsPlugin == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_PLUGIN);
			}
			this.data.partitionsPlugin = partitionsPlugin;
			return this;
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
	 * needed to adequately test the {@link PartitionsPlugin} by generating:
	 * <ul>
	 * <li>{@link AttributesPluginData}</li>
	 * <li>{@link PartitionsPlugin}</li>
	 * <li>{@link PeoplePluginData}</li>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardAttributesPluginData}</li>
	 * <li>{@link #getStandardPartitionsPlugin}</li>
	 * <li>{@link #getStandardPeoplePluginData}</li>
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setAttributesPluginData}</li>
	 * <li>{@link Factory#setPartitionsPlugin}</li>
	 * <li>{@link Factory#setPeoplePluginData}</li>
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption {@linkplain NucleusError#NULL_PLUGIN_DATA} if
	 *                           testPluginData is null
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(initialPopulation, seed, testPluginData));
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link PartitionsPlugin} by generating:
	 * <ul>
	 * <li>{@link AttributesPluginData}</li>
	 * <li>{@link PartitionsPlugin}</li>
	 * <li>{@link PeoplePluginData}</li>
	 * <li>{@link StochasticsPluginData}</li>
	 * </ul>
	 * either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardAttributesPluginData}</li>
	 * <li>{@link #getStandardPartitionsPlugin}</li>
	 * <li>{@link #getStandardPeoplePluginData}</li>
	 * <li>{@link #getStandardStochasticsPluginData}</li>
	 * </ul>
	 * or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setAttributesPluginData}</li>
	 * <li>{@link Factory#setPartitionsPlugin}</li>
	 * <li>{@link Factory#setPeoplePluginData}</li>
	 * <li>{@link Factory#setStochasticsPluginData}</li>
	 * </ul>
	 * via the {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
	 *                           if consumer is null
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
	 * Returns a standardized AttributesPluginData that is minimally adequate for
	 * testing the PartitionsPlugin
	 * The resulting AttributesPluginData will include:
	 * <ul>
	 * <li>Every AttributeId included in {@link TestAttributeId}</li>
	 * <ul>
	 * <li>along with the attributeDefinition for each</li>
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
	 * The resulting PartitionsPlugin will include:
	 * <ul>
	 * <li>the basic PartitionsPlugin from
	 * {@link PartitionsPlugin#getPartitionsPlugin}</li>
	 * <li>An additional pluginDependency on {@link AttributesPluginId}</li>
	 * </ul>
	 */
	public static Plugin getStandardPartitionsPlugin() {

		return PartitionsPlugin.builder()//
				.addPluginDependency(AttributesPluginId.PLUGIN_ID)//
				.setPartitionsPluginData(PartitionsPluginData.builder().build())//
				.getPartitionsPlugin();
	}

	/**
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the PartitionsPlugin
	 * The resulting PeoplePluginData will include:
	 * <ul>
	 * <li>a number of people equal to the passed in intialPopulation</li>
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		if (initialPopulation > 0) {
			peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
		}
		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate for
	 * testing the PartitionsPlugin
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
