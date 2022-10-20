package plugins.regions.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDefinitionInitialization;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportId;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionPropertyReport.class)
public class AT_RegionPropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {ActorContext.class})
	public void testInit() {

		/*
		 * We will add three regions, one agent and the region property report
		 * to the engine. The regions will have a few properties and the agent
		 * will alter various region properties over time. Report items from the
		 * report will be collected in an output consumer. The expected report
		 * items will be collected in a separate consumer and the consumers will
		 * be compared for equality. The output consumers properly accounts for
		 * report item duplications.
		 */

		Experiment.Builder builder = Experiment.builder();

		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();

		// add regions A, B and C
		RegionId regionA = new SimpleRegionId("Region_A");
		regionBuilder.addRegion(regionA);
		RegionId regionB = new SimpleRegionId("Region_B");
		regionBuilder.addRegion(regionB);
		RegionId regionC = new SimpleRegionId("Region_C");
		regionBuilder.addRegion(regionC);
		
		RegionId regionD = new SimpleRegionId("Region_D");

		// add the region properties
		RegionPropertyId prop_age = new SimpleRegionPropertyId("prop_age");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		regionBuilder.defineRegionProperty(prop_age, propertyDefinition);

		RegionPropertyId prop_infected = new SimpleRegionPropertyId("prop_infected");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build();
		regionBuilder.defineRegionProperty(prop_infected, propertyDefinition);

		RegionPropertyId prop_length = new SimpleRegionPropertyId("prop_length");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(10.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty(prop_length, propertyDefinition);

		RegionPropertyId prop_height = new SimpleRegionPropertyId("prop_height");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(5.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty(prop_height, propertyDefinition);

		RegionPropertyId prop_policy = new SimpleRegionPropertyId("prop_policy");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue("start").setType(String.class).build();
		regionBuilder.defineRegionProperty(prop_policy, propertyDefinition);
		
		RegionPropertyId prop_vaccine = new SimpleRegionPropertyId("prop_vaccine");
		

		builder.addPlugin(RegionsPlugin.getRegionsPlugin(regionBuilder.build()));

		// add the report
		ReportsPluginData reportsPluginData = ReportsPluginData	.builder()//
																.addReport(() -> new RegionPropertyReport(REPORT_ID)::init)//
																.build();//

		builder.addPlugin(ReportsPlugin.getReportsPlugin(reportsPluginData));

		// add remaining plugins
		builder.addPlugin(PeoplePlugin.getPeoplePlugin(PeoplePluginData.builder().build()));
		builder.addPlugin(StochasticsPlugin.getStochasticsPlugin(StochasticsPluginData.builder().setSeed(8833508541323194123L).build()));

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various region properties at
		// various times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "move");
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 45);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 45);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 100);
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 13.6);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "hold");
			RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(regionD).build();
			regionsDataManager.addRegion(regionConstructionData);

			PropertyDefinition def = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization.builder().setPropertyDefinition(def).setRegionPropertyId(prop_vaccine).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
			
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "terminate");
			regionsDataManager.setRegionPropertyValue(regionA, prop_vaccine, 5);

			// note the duplicated value

			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 99.7);
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 99.7);

			// and now a third setting of the same property to a new value
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 100.0);
			regionsDataManager.setRegionPropertyValue(regionB, prop_length, 60.0);
			
			regionsDataManager.setRegionPropertyValue(regionD, prop_height, 70.0);
			regionsDataManager.setRegionPropertyValue(regionD, prop_length, 45.0);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();

		TestReportItemOutputConsumer reportItemOutputConsumer = new TestReportItemOutputConsumer();
		builder.addExperimentContextConsumer(reportItemOutputConsumer::init);
		builder.addExperimentContextConsumer(experimentPlanCompletionObserver::init);
		builder.reportProgressToConsole(false);
		builder.reportFailuresToConsole(false);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).isPresent());
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete());

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		expectedReportItems.put(getReportItem(0.0, regionA, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(1.0, regionA, prop_age, 45), 2);	
		expectedReportItems.put(getReportItem(2.0, regionA, prop_age, 100), 1);
		expectedReportItems.put(getReportItem(2.0, regionB, prop_height, 13.6), 1);
		expectedReportItems.put(getReportItem(2.0, regionC, prop_policy, "hold"), 1);
		expectedReportItems.put(getReportItem(3.0, regionC, prop_policy, "terminate"), 1);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_height, 99.7), 2);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_height, 100.0), 1);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_length, 60.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_policy, "move"), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionA, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(2.0, regionB, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(2.0, regionC, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(3.0, regionA, prop_vaccine, 5), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(3.0, regionD, prop_height, 70.0), 1);
		expectedReportItems.put(getReportItem(3.0, regionD, prop_length, 45.0), 1);


		Map<ReportItem, Integer> actualReportItems = reportItemOutputConsumer.getReportItems().get(0);
		
		assertEquals(expectedReportItems, actualReportItems);
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

	private static final ReportId REPORT_ID = new SimpleReportId("region property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Region").add("Property").add("Value").build();
}
