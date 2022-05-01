package plugins.partitions.testsupport;

import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.AttributesPluginData;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.errors.ContractException;

public class PartitionsActionSupport {

	public static void testConsumer(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		testConsumers(initialPopulation, seed, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, Plugin testPlugin) {

		final Builder builder = Simulation.builder();
		builder.addPlugin(testPlugin);

		// define some person attributes
		final AttributesPluginData.Builder attributesBuilder = AttributesPluginData.builder();
		for (final TestAttributeId testAttributeId : TestAttributeId.values()) {
			attributesBuilder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = attributesBuilder.build();
		Plugin attributesPlugin = AttributesPlugin.getAttributesPlugin(attributesPluginData);
		builder.addPlugin(attributesPlugin);
		
		

		//add the people plugin

		final PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		//add the report plugin
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build());
		builder.addPlugin(reportPlugin);

		//add the stochastics plugin
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(seed).build());
		builder.addPlugin(stochasticsPlugin);

		//add the partitions plugin
		Plugin partitionsPlugin = PartitionsPlugin.getPartitionsPlugin();
		builder.addPlugin(partitionsPlugin);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		builder	.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput)//
				.build()//
				.execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}
	}

}
