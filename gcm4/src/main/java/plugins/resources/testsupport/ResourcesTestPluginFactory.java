package plugins.resources.testsupport;

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
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain ResourcesPlugin}. Provides
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
public class ResourcesTestPluginFactory {

	private ResourcesTestPluginFactory() {
	}

	private static class Data {
		private ResourcesPluginData resourcesPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, long seed, TestPluginData testPluginData) {
			this.resourcesPluginData = getStandardResourcesPluginData(seed);
			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
			this.regionsPluginData = getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), seed);
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
		 * Method that will get the PluginData for the Resources, Regions,
		 * People, Stochastic and
		 * Test Plugins
		 * and use the respective PluginData to build Plugins
		 * 
		 * @return a List containing a ResourcesPlugin, PeoplePlugin,
		 *         RegionsPlugin, StochasticsPlugin and
		 *         a TestPlugin
		 * 
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(this.data.resourcesPluginData);
			// add the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(this.data.regionsPluginData);

			// add the stochastics plugin
			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(resourcesPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(regionsPlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Method to set the ResourcesPluginData in this Factory.
		 * 
		 * @param resourcesPluginData the ResourcesPluginData you want to use, if
		 *                            different
		 *                            from the standard PluginData
		 * @return an instance of this Factory
		 * 
		 */
		public Factory setResourcesPluginData(ResourcesPluginData resourcesPluginData) {
			this.data.resourcesPluginData = resourcesPluginData;
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
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link ResourcesPlugin} by generating:
	 * <p>
	 * {@link ResourcesPluginData}
	 * <p>
	 * {@link RegionsPluginData}
	 * <p>
	 * {@link PeoplePluginData}
	 * <p>
	 * {@link StochasticsPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardResourcesPluginData},
	 * <p>
	 * {@link #getStandardPeoplePluginData},
	 * <p>
	 * {@link #getStandardRegionsPluginData},
	 * <p>
	 * {@link #getStandardStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setResourcesPluginData},
	 * <p>
	 * {@link Factory#setPeoplePluginData},
	 * <p>
	 * {@link Factory#setRegionsPluginData},
	 * <p>
	 * {@link Factory#setStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @param initialPopulation the initial number of people in the simulation
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
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link ResourcesPlugin} by generating:
	 * <p>
	 * {@link ResourcesPluginData}
	 * <p>
	 * {@link RegionsPluginData}
	 * <p>
	 * {@link PeoplePluginData}
	 * <p>
	 * {@link StochasticsPluginData}
	 * <p>
	 * either directly (by default)
	 * <p>
	 * (
	 * <p>
	 * {@link #getStandardResourcesPluginData},
	 * <p>
	 * {@link #getStandardPeoplePluginData},
	 * <p>
	 * {@link #getStandardRegionsPluginData},
	 * <p>
	 * {@link #getStandardStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * or explicitly set
	 * <p>
	 * (
	 * <p>
	 * {@link Factory#setResourcesPluginData},
	 * <p>
	 * {@link Factory#setPeoplePluginData},
	 * <p>
	 * {@link Factory#setRegionsPluginData},
	 * <p>
	 * {@link Factory#setStochasticsPluginData}
	 * <p>
	 * )
	 * </p>
	 * 
	 * via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @param initialPopulation the initial population of the simulation
	 * @param seed              used to seed a RandomGenerator
	 * @param testPluginData    PluginData that will be used to generate a Test
	 *                          Plugin
	 * @return a new instance of Factory
	 * 
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
		return new Factory(new Data(initialPopulation, seed, testPluginData));
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

	public static ResourcesPluginData getStandardResourcesPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		ResourcesPluginData.Builder resourcesBuilder = ResourcesPluginData.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId);
			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}

		return resourcesBuilder.build();
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
