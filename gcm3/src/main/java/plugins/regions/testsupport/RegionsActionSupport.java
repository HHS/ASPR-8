package plugins.regions.testsupport;

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
import nucleus.util.ContractException;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.TimeTrackingPolicy;

public final class RegionsActionSupport {
	public static void testConsumer(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy, Consumer<ActorContext> consumer) {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		testConsumers(initialPopulation, seed, timeTrackingPolicy, testPlugin);
	}

	public static void testConsumers(int initialPopulation, long seed, TimeTrackingPolicy timeTrackingPolicy, Plugin testPlugin) {

		Builder builder = Simulation.builder();

		// add the region plugin
		RegionPluginData.Builder regionPluginBuilder = RegionPluginData.builder();
		for (TestRegionId regionId : TestRegionId.values()) {
			regionPluginBuilder.addRegion(regionId);
		}
		
		for(TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			regionPluginBuilder.defineRegionProperty(testRegionPropertyId, testRegionPropertyId.getPropertyDefinition());
		}
		
		regionPluginBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
		builder.addPlugin(RegionPlugin.getRegionPlugin(regionPluginBuilder.build()));

		// add the people plugin
		TestRegionId testRegionId = TestRegionId.REGION_1;
		BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
		for (int i = 0; i < initialPopulation; i++) {
			PersonConstructionData personConstructionData = PersonConstructionData.builder().add(testRegionId).build();
			testRegionId = testRegionId.next();
			bulkBuilder.add(personConstructionData);
		}
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().addBulkPersonConstructionData(bulkBuilder.build()).build();
		builder.addPlugin(PeoplePlugin.getPeoplePlugin(peoplePluginData));

		// add the report plugin
		builder.addPlugin(ReportsPlugin.getReportPlugin(ReportsPluginData.builder().build()));

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.getPlugin(StochasticsPluginData.builder().setSeed(seed).build()));

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the test plugin
		builder.addPlugin(testPlugin);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();

		// build and execute the engine
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput);
		builder.build().execute();

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

}
