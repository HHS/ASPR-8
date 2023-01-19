package plugins.partitions.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
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
import util.errors.ContractException;

public class PartitionsActionSupport {

	private PartitionsActionSupport() {
	}

	public static void testConsumer(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		testConsumers(initialPopulation, seed, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, Plugin testPlugin) {

		Builder builder = Simulation.builder();

		for (Plugin plugin : setUpPluginsForTest(initialPopulation)) {
			builder.addPlugin(plugin);
		}

		// add the stochastics plugin
		Plugin stochasticsPlugin = StochasticsPlugin
				.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(seed).build());
		builder.addPlugin(stochasticsPlugin);

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		// build and execute the engine
		builder.addPlugin(testPlugin)
				.setOutputConsumer(outputConsumer)//
				.build()//
				.execute();

		// show that all actions were executed
		if (!outputConsumer.isComplete()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

	public static List<Plugin> setUpPluginsForTest(int initialPopulation) {
		AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = attributesBuilder.build();
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesPluginData);

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}

		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		Plugin partitionsPlugin = PartitionsPlugin.getPartitionsPlugin(AttributesPluginId.PLUGIN_ID);

		return setUpPluginsForTest(attributesPlugin, peoplePlugin, partitionsPlugin);
	}

	public static List<Plugin> setUpPluginsForTest(Plugin attributesPlugin,
			Plugin peoplePlugin,
			Plugin partitionsPlugin) {

		List<Plugin> pluginsToAdd = new ArrayList<>();
		pluginsToAdd.add(attributesPlugin);
		pluginsToAdd.add(peoplePlugin);
		pluginsToAdd.add(partitionsPlugin);
		return pluginsToAdd;
	}

}
