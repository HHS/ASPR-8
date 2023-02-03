package plugins.personproperties.testsupport;

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
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.random.RandomGeneratorProvider;

public class PersonPropertiesTestPluginFactory {

	private PersonPropertiesTestPluginFactory() {
	}

	private static class Data {
		private PersonPropertiesPluginData personPropertiesPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, long seed, TestPluginData testPluginData) {

			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
			this.personPropertiesPluginData = PersonPropertiesTestPluginFactory
					.getStandardPersonPropertiesPluginData(this.peoplePluginData.getPersonIds(), seed);
			this.regionsPluginData = PersonPropertiesTestPluginFactory
					.getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), seed);
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
		 * Method that will get the PluginData for the PersonProperties, Regions,
		 * People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a PersonPropertiesPlugin, PeoplePlugin,
		 *         RegionsPlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin personPropertiesPlugin = PersonPropertiesPlugin
					.getPersonPropertyPlugin(this.data.personPropertiesPluginData);

			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(this.data.regionsPluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(personPropertiesPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(regionsPlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the PersonPropertiesPluginData in this Factory.
		 * 
		 * @param personPropertiesPluginData the PersonPropertiesPluginData you want to
		 *                                   use, if
		 *                                   different
		 *                                   from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setPersonPropertiesPluginData(PersonPropertiesPluginData personPropertiesPluginData) {
			this.data.personPropertiesPluginData = personPropertiesPluginData;
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
	 * Method that will generate PersonPropertiesPluginData, PeoplePluginData,
	 * RegionsPluginData, StocasticsPluginData and TestPluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation how many people are in the simulation at the start
	 * @param seed              used to seed a RandomGenerator
	 * @param testPluginData    PluginData that will be used to generate a
	 *                          TestPlugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
		return new Factory(new Data(initialPopulation, seed, testPluginData));
	}

	/**
	 * Method that will generate PersonPropertiesPluginData, PeoplePluginData,
	 * RegionsPluginData, StocasticsPluginData and TestPluginData based on some
	 * configuration parameters.
	 * 
	 * @param initialPopulation how many people are in the simulation at the
	 *                          start
	 * @param seed              used to seed a RandomGenerator
	 * @param consumer          consumer to use to generate TestPluginData
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, Consumer<ActorContext> consumer) {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(initialPopulation, seed, testPluginData);
	}

	/**
	 * Method that will return a Standard PersonPropertiesPluginData based on some
	 * configuration parameters.
	 * 
	 * @param people a list of people in the simulation
	 * @param seed   used to seed a RandomGenerator
	 * @return the resulting PersonPropertiesPluginData
	 * 
	 */
	public static PersonPropertiesPluginData getStandardPersonPropertiesPluginData(List<PersonId> people, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition());
		}
		for (PersonId personId : people) {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean doesNotHaveDefaultValue = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (doesNotHaveDefaultValue || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}
		return personPropertyBuilder.build();
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
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		return peopleBuilder.build();
	}

	/**
	 * Method that will return a Standard RegionsPluginData based on some
	 * configuration parameters.
	 * 
	 * @param people a list of people that are in the simulation
	 * @param seed   used to seed a Random Generator
	 * @return the resulting RegionsPluginData
	 * 
	 */
	public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();
		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionBuilder.setPersonRegion(personId, randomRegionId);
		}
		return regionBuilder.build();

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
