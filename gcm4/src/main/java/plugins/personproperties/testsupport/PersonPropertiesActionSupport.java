package plugins.personproperties.testsupport;

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
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class PersonPropertiesActionSupport {

	private PersonPropertiesActionSupport() {
	}

	public static void testConsumer(int initialPopulation, long seed, Consumer<ActorContext> consumer) {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, seed, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, Plugin testPlugin) {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(initialPopulation, seed)) {
			builder.addPlugin(plugin);
		}

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()
				.setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticPlugin);

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

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));

		}

		// add the person property plugin
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition());
		}
		for (PersonId personId : people) {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
			}
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

		// add the regions plugin
		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();
		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionBuilder.setPersonRegion(personId, randomRegionId);
		}
		RegionsPluginData regionsPluginData = regionBuilder.build();
		Plugin regionPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		return setUpPluginsForTest(personPropertyPlugin, regionPlugin, peoplePlugin);
	}

	public static List<Plugin> setUpPluginsForTest(Plugin personPropertyPlugin, Plugin regionPlugin,
			Plugin peoplePlugin) {
		List<Plugin> pluginsToAdd = new ArrayList<>();

		pluginsToAdd.add(personPropertyPlugin);
		pluginsToAdd.add(regionPlugin);
		pluginsToAdd.add(peoplePlugin);
		return pluginsToAdd;
	}
}
