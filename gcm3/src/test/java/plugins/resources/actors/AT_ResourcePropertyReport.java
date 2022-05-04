package plugins.resources.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportId;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = ResourcePropertyReport.class)
public class AT_ResourcePropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8914112012010329946L);
		int initialPopulation = 20;

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Experiment.Builder builder = Experiment.builder();

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
		builder.addPlugin(resourcesPlugin);

		// add the people plugin

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);
		
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
		builder.addPlugin(regionPlugin);

		// add the report plugin

		ReportsPluginData.Builder reportsBuilder = ReportsPluginData.builder();
		reportsBuilder.addReport(() -> new ResourcePropertyReport(REPORT_ID)::init);
		ReportsPluginData reportsPluginData = reportsBuilder.build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);


		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// // create an agent and have it assign various resource properties at
		// // various times
		

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {			
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, "A");
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 45);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 36.7);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true);
			

			// note the duplicated value
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5);
			

			// and now a third setting of the same property to a new value
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 100);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 60);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		builder.addOutputHandler(testReportItemOutputConsumer::init);
		builder.addOutputHandler(experimentPlanCompletionObserver::init);
		builder.reportProgressToConsole(false);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete());

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		Map<ReportItem, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 1673029105),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 0.9762970538942173),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 1818034648),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, 319183829),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE, 704893369),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_DOUBLE_IMMUTABLE, 0.7547798894049567),1);
		expectedMap.put(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, "A"),1);
		expectedMap.put(getReportItem(1.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 45),1);
		expectedMap.put(getReportItem(1.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 36.7),1);
		expectedMap.put(getReportItem(2.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(2.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, false),1);
		expectedMap.put(getReportItem(3.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true),1);
		expectedMap.put(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5),2);
		expectedMap.put(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 100),1);
		expectedMap.put(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 60),1);

		Map<ReportItem, Integer> actualMap = testReportItemOutputConsumer.getReportItems().get(0);
		
		
		assertEquals(expectedMap, actualMap);

	}

	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("resource property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("time").add("resource").add("property").add("value").build();
}
