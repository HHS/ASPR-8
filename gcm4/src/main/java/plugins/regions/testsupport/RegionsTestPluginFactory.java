package plugins.regions.testsupport;

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
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.regions.support.RegionError;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain RegionsPlugin}. Provides
 * convenience methods for obtaining standarized PluginData for the listed
 * Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
 * 
 */
public final class RegionsTestPluginFactory {

	private RegionsTestPluginFactory() {
	}

	private static class Data {
		private RegionPropertyReportPluginData regionPropertyReportPluginData;
		private RegionTransferReportPluginData regionTransferReportPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, TimeTrackingPolicy timeTrackingPolicy, long seed, TestPluginData testPluginData) {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
			this.regionsPluginData = getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), timeTrackingPolicy, randomGenerator.nextLong());
			this.stochasticsPluginData = getStandardStochasticsPluginData(randomGenerator.nextLong());
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
		 * Returns a list of plugins containing a People, Regions, Stochastics
		 * and Test Plugin built from the contributed PluginDatas.
		 * 
		 * {@link RegionsTestPluginFactory#getStandardRegionsPluginData}
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link RegionsTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link RegionsTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link RegionsTestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin regionsPlugin = RegionsPlugin.builder()//
												.setRegionsPluginData(this.data.regionsPluginData)//
												.setRegionPropertyReportPluginData(this.data.regionPropertyReportPluginData)//
												.setRegionTransferReportPluginData(this.data.regionTransferReportPluginData)//
												.getRegionsPlugin();

			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(regionsPlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
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
		 * Sets the {@link RegionPropertyReportPluginData} in this Factory. This
		 * explicit instance of pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain RegionError#NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setRegionPropertyReportPluginData(RegionPropertyReportPluginData regionPropertyReportPluginData) {
			if (regionPropertyReportPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA);
			}
			this.data.regionPropertyReportPluginData = regionPropertyReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link RegionTransferReportPluginData} in this Factory. This
		 * explicit instance of pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain RegionError#NULL_REGION_TRANSFER_REPORT_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setRegionTransferReportPluginData(RegionTransferReportPluginData regionTransferReportPluginData) {
			if (regionTransferReportPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_TRANSFER_REPORT_PLUGIN_DATA);
			}
			this.data.regionTransferReportPluginData = regionTransferReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link RegionsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a RegionsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain RegionError#NULL_REGION_PLUGIN_DATA} if the
		 *             passed in pluginData is null
		 */
		public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
			if (regionsPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
			}
			this.data.regionsPluginData = regionsPluginData;
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
	 * plugins needed to adequately test the {@link RegionsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_PLUGIN_DATA} if testPluginData
	 *             is null
	 */
	public static Factory factory(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}

		return new Factory(new Data(initialPopulation, timeTrackingPolicy, seed, testPluginData));

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link RegionsPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if
	 *             consumer is null
	 */
	public static Factory factory(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy, Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(initialPopulation, seed, timeTrackingPolicy, testPluginData);
	}

	/**
	 * Returns a standardized RegionsPluginData that is minimally adequate for
	 * testing the RegionsPlugin
	 * <li>The resulting RegionsPluginData will include:
	 * <ul>
	 * <li>Every RegionId in {@link TestRegionId}
	 * <li>Every RegionPropertyId in {@link TestRegionPropertyId}
	 * <ul>
	 * <li>along with the propertyDefinition for each.
	 * <li>If the propertyDefinition has a default value, that value is used.
	 * Otherwise a randomPropertyValue is set using a RandomGenerator seeded by
	 * the passed in seed via
	 * {@link TestRegionPropertyId#getRandomPropertyValue}
	 * </ul>
	 * <li>the passed in timeTrackingPolicy
	 * <li>Every person in the passed in list will be added to a RegionId
	 * <ul>
	 * <li>starting with RegionId_1 and looping through all possible RegionIds
	 * in {@link TestRegionId}
	 * </ul>
	 * </ul>
	 */
	public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people, TimeTrackingPolicy timeTrackingPolicy, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
			if (propertyDefinition.getDefaultValue().isEmpty()) {
				for (TestRegionId regionId : TestRegionId.values()) {
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, testRegionPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}

		}
		TestRegionId testRegionId = TestRegionId.REGION_1;
		regionPluginBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
		for (PersonId personId : people) {
			regionPluginBuilder.setPersonRegion(personId, testRegionId);
			testRegionId = testRegionId.next();
		}
		return regionPluginBuilder.build();
	}

	/**
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the RegionsPlugin
	 * <li>The resulting PeoplePluginData will include:
	 * <ul>
	 * <li>a number of people equal to the passed in intialPopulation
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		if (initialPopulation > 0) {
			peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
		}
		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate
	 * for testing the RegionsPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		WellState wellState = WellState.builder().setSeed(seed).build();
		return StochasticsPluginData.builder().setMainRNGState(wellState).build();
	}
}
