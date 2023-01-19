package plugins.regions.testsupport;

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
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public final class RegionsActionSupport {

	private RegionsActionSupport() {
	}

	public static void testConsumer(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy,
			Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, seed, timeTrackingPolicy, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy,
			Plugin testPlugin) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(initialPopulation, seed, timeTrackingPolicy)) {
			builder.addPlugin(plugin);
		}

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin
				.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build()));

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		// build and execute the engine
		builder.addPlugin(testPlugin)
				.setOutputConsumer(outputConsumer)
				.build()
				.execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	public static List<Plugin> setUpPluginsForTest(int initialPopulation, long seed,
			TimeTrackingPolicy timeTrackingPolicy) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		// add the region plugin
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

		Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionPluginBuilder.build());

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		return setUpPluginsForTest(regionsPlugin, peoplePlugin);
	}

	public static List<Plugin> setUpPluginsForTest(Plugin regionPlugin, Plugin peoplePlugin) {
		List<Plugin> pluginsToAdd = new ArrayList<>();

		pluginsToAdd.add(regionPlugin);
		pluginsToAdd.add(peoplePlugin);
		return pluginsToAdd;
	}

}
