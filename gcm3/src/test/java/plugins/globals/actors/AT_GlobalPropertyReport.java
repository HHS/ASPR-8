package plugins.globals.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.globals.GlobalDataManager;
import plugins.globals.GlobalPlugin;
import plugins.globals.GlobalPluginData;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportId;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GlobalPropertyReport.class)
public class AT_GlobalPropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {

		/*
		 * We will add one agent and the global property report to the engine.
		 * We will define a few global properties and the agent will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality. The output consumers properly accounts for
		 * report item duplications.
		 */

		Experiment.Builder builder = Experiment.builder();
		builder.setExperimentProgressConsole(false);
		builder.setReportScenarioFailureToConsole(false);

		// add the global property definitions
		GlobalPluginData.Builder initialDatabuilder = GlobalPluginData.builder();

		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		GlobalPluginData globalPluginData = initialDatabuilder.build();
		builder.addPlugin(GlobalPlugin.getPlugin(globalPluginData));

		// add the report
		ReportsPluginData reportsInitialData = ReportsPluginData.builder()//
																.addReport(() -> new GlobalPropertyReport(REPORT_ID)::init)//
																.build();//
		builder.addPlugin(ReportsPlugin.getReportPlugin(reportsInitialData));

		

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various global properties at
		// various times

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			GlobalDataManager globalDataManager = c.getDataManager(GlobalDataManager.class);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_1, 67);			
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			GlobalDataManager globalDataManager = c.getDataManager(GlobalDataManager.class);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_2, 88.88);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_3, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			GlobalDataManager globalDataManager = c.getDataManager(GlobalDataManager.class);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_1, 100);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_2, 3.45);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			GlobalDataManager globalDataManager = c.getDataManager(GlobalDataManager.class);
			
			
			globalDataManager.setGlobalPropertyValue(globalPropertyId_3, false);			
			// note the duplicated value
			globalDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			// and now a third setting of the same property to a new value
			globalDataManager.setGlobalPropertyValue(globalPropertyId_2, 100.0);
			globalDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the experiment
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();

		builder.addOutputHandler(testReportItemOutputConsumer::init);
		builder.addOutputHandler(experimentPlanCompletionObserver::init);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete());

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		expectedReportItems.put(getReportItem(0.0, globalPropertyId_1, 3), 1);
		expectedReportItems.put(getReportItem(0.0, globalPropertyId_2, 6.78), 1);
		expectedReportItems.put(getReportItem(0.0, globalPropertyId_3, true), 1);
		expectedReportItems.put(getReportItem(0.0, globalPropertyId_1, 67), 1);
		expectedReportItems.put(getReportItem(1.0, globalPropertyId_2, 88.88), 1);
		expectedReportItems.put(getReportItem(1.0, globalPropertyId_3, false), 1);
		expectedReportItems.put(getReportItem(2.0, globalPropertyId_1, 100), 1);
		expectedReportItems.put(getReportItem(2.0, globalPropertyId_2, 3.45), 1);
		expectedReportItems.put(getReportItem(2.0, globalPropertyId_3, true), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_3, false), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_2, 99.7), 2);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_2, 100.0), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_3, true), 1);

		Map<ReportItem, Integer> actualReportItems = testReportItemOutputConsumer.getReportItems().get(0);
		
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

	private static final ReportId REPORT_ID = new SimpleReportId("global property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("time").add("property").add("value").build();
}
