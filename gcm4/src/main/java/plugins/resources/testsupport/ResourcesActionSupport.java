package plugins.resources.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
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
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the resources plugin. Provides convenience
 * methods for integrating an action plugin into a resource-based simulation
 * test harness.
 * 
 * 
 *
 */
public class ResourcesActionSupport {

	private ResourcesActionSupport() {
	}

	/**
	 * Creates an action plugin with an agent that will execute the given
	 * consumer at time 0. The action plugin and the remaining arguments are
	 * passed to an invocation of the testConsumers() method.
	 */
	public static void testConsumer(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, seed, testPlugin);
	}

	/**
	 * Executes a simulation instance that supports resources plugin testing.
	 * 
	 * The initial population is added in the initial data.
	 * 
	 * Resources and their property definitions and initial values are added. No
	 * resources are allocated to regions or people.
	 * 
	 * The seed is used to produce randomized initial group types and group
	 * memberships.
	 * 
	 * The action plugin is integrated into the simulation run and must contain
	 * at least one action plan. This helps to ensure that a test that does not
	 * run completely does not lead to a false positive test evaluation.
	 * 
	 * @throws ContractException
	 *                           <li>{@linkplain ActionError#ACTION_EXECUTION_FAILURE}
	 *                           if not
	 *                           all action plans execute or if there are no action
	 *                           plans
	 *                           contained in the action plugin</li>
	 */
	public static void testConsumers(int initialPopulation, long seed, Plugin testPlugin) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(initialPopulation, seed)) {
			builder.addPlugin(plugin);
		}

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		// build and execute the engine
		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		builder.addPlugin(testPlugin)
				.setOutputConsumer(outputConsumer)
				.build()
				.execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

	public static List<Plugin> setUpPluginsForTest(int initialPopulation, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Plugin> pluginsToAdd = new ArrayList<>();

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the resources plugin
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

		ResourcesPluginData resourcesPluginData = resourcesBuilder.build();
		Plugin resourcesPlugin = ResourcesPlugin.getResourcesPlugin(resourcesPluginData);
		pluginsToAdd.add(resourcesPlugin);

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		pluginsToAdd.add(peoplePlugin);

		// add the regions plugin
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			regionsBuilder.setPersonRegion(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}

		RegionsPluginData regionsPluginData = regionsBuilder.build();
		Plugin regionPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);

		pluginsToAdd.add(regionPlugin);

		return setUpPluginsForTest(resourcesPlugin, peoplePlugin, regionPlugin);
	}

	public static List<Plugin> setUpPluginsForTest(Plugin resourcesPlugin, Plugin peoplePlugin, Plugin regionPlugin) {
		List<Plugin> pluginsToAdd = new ArrayList<>();

		pluginsToAdd.add(resourcesPlugin);
		pluginsToAdd.add(regionPlugin);
		pluginsToAdd.add(peoplePlugin);
		return pluginsToAdd;
	}
}
