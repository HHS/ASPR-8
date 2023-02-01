package plugins.regions.testsupport;

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
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public final class RegionsTestPluginFactory {

	private RegionsTestPluginFactory() {
	}

	private static class Data {
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, TimeTrackingPolicy timeTrackingPolicy, long seed,
				TestPluginData testPluginData) {

			this.peoplePluginData = RegionsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);
			this.regionsPluginData = RegionsTestPluginFactory
					.getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), timeTrackingPolicy, seed);
			this.stochasticsPluginData = RegionsTestPluginFactory.getStandardStochasticsPluginData(seed);
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
		 * Method that will get the PluginData for the Regions,
		 * People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a PeoplePlugin,
		 *         RegionsPlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(this.data.regionsPluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(regionsPlugin);
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
		 * Method to set the RegionsPluginData in this Factory.
		 * 
		 * @param regionsPluginData the RegionsPluginData you want to use, if different
		 *                          from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
			this.data.regionsPluginData = regionsPluginData;
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
	 * Method that will generate PeoplePluginData,
	 * RegionsPluginData, StocasticsPluginData and TestPluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation  the initial size of the population in the
	 *                           simulation
	 * @param seed               used to seed a RandomGenerator
	 * @param timeTrackingPolicy time tracking policy
	 * @param consumer           consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy,
			Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(initialPopulation, seed, timeTrackingPolicy, testPluginData);
	}

	/**
	 * Method that will generate PeoplePluginData,
	 * RegionsPluginData, StocasticsPluginData and TestPluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation  the initial size of the population in the
	 *                           simulation
	 * @param seed               used to seed a RandomGenerator
	 * @param timeTrackingPolicy time tracking policy
	 * @param testPlugindData    PluginData that will be used to generate a
	 *                           TestPlugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy,
			TestPluginData testPlugindData) {

		return new Factory(new Data(initialPopulation, timeTrackingPolicy, seed, testPlugindData));

	}

	/**
	 * Method that will return a Standard RegionsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param people             a list of people that are in the simulation
	 * @param timeTrackingPolicy time tracking policy
	 * @param seed               used to seed a Random Generator
	 * @return the resulting RegionsPluginData
	 * 
	 */
	public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people,
			TimeTrackingPolicy timeTrackingPolicy, long seed) {
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
					regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
							testRegionPropertyId.getRandomPropertyValue(randomGenerator));
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
	 * Method that will return a Standard PeoplePluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation how many people should be in the simulation at the
	 *                          start
	 * @return the resulting PeoplePluginData
	 * 
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
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
