package plugins.personproperties.testsupport;

import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.RandomGeneratorProvider;

public class PersonPropertiesActionSupport {

	public static void testConsumer(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, seed, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, Plugin testPlugin) {

		Builder builder = Simulation.builder();

		// add the person property plugin
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
		builder.addPlugin(personPropertyPlugin);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the regions plugin
		RegionPluginData.Builder regionBuilder = RegionPluginData.builder();
		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.addRegion(testRegionId);
		}
		RegionPluginData regionPluginData = regionBuilder.build();
		Plugin regionPlugin = RegionPlugin.getRegionPlugin(regionPluginData);
		builder.addPlugin(regionPlugin);

		// add the report plugin
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(seed).build();
		Plugin stochasticPlugin = StochasticsPlugin.getPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticPlugin);

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			personBuilder.add(randomRegionId);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(testPersonPropertyId,randomPropertyValue);
				personBuilder.add(personPropertyInitialization);
			}
			PersonConstructionData personConstructionData = personBuilder.build();
			bulkBuilder.add(personConstructionData);
		}
		BulkPersonConstructionData bulkPersonConstructionData = bulkBuilder.build();
		peopleBuilder.addBulkPersonConstructionData(bulkPersonConstructionData);
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		// add the test plugin
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}
}
