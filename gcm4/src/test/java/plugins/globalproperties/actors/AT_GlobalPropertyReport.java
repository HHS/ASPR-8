package plugins.globalproperties.actors;

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
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.GlobalPropertyInitialization;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.*;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = GlobalPropertyReport.class)
public class AT_GlobalPropertyReport {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(name = "init", args = {ActorContext.class})
	public void testInit() {

		/*
		 * We will add one actor and the global property report to the engine.
		 * We will define a few global properties and the actor will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality.
		 */

		Experiment.Builder builder = Experiment.builder();
		builder.reportProgressToConsole(false);
		builder.reportFailuresToConsole(false);

		// add the global property definitions
		GlobalPropertiesPluginData.Builder initialDatabuilder = GlobalPropertiesPluginData.builder();

		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		GlobalPropertiesPluginData globalPropertiesPluginData = initialDatabuilder.build();
		builder.addPlugin(GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData));

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		GlobalPropertyId globalPropertyId_4 = new SimpleGlobalPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		GlobalPropertyId globalPropertyId_5 = new SimpleGlobalPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

		// add the report

		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			GlobalPropertyReport globalPropertyReport = GlobalPropertyReport.builder()//
																			.setReportId(REPORT_ID)//
																			.includeAllExtantPropertyIds(true)//
																			.includeNewPropertyIds(true)//
																			.build();
			return globalPropertyReport::init;
		}).build();
		builder.addPlugin(ReportsPlugin.getReportsPlugin(reportsPluginData));

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various global properties at
		// various times

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_1, 67);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 88.88);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_1, 100);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 3.45);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder().setGlobalPropertyId(globalPropertyId_4).setPropertyDefinition(propertyDefinition_4)
																									.build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);

			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, false);
			// note the duplicated value
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			// and now a third setting of the same property to a new value
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 100.0);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder().setGlobalPropertyId(globalPropertyId_5).setPropertyDefinition(propertyDefinition_5)
																									.build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the experiment
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();

		builder.addExperimentContextConsumer(testReportItemOutputConsumer::init);
		builder.addExperimentContextConsumer(experimentPlanCompletionObserver::init);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).isPresent());
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
		expectedReportItems.put(getReportItem(2.0, globalPropertyId_4, true), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_3, false), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_2, 99.7), 2);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_2, 100.0), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_3, true), 1);
		expectedReportItems.put(getReportItem(3.0, globalPropertyId_5, 199.16), 1);

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

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "build", args = {})
	public void testBuild() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		ReportId reportId = new SimpleReportId(1000);
		GlobalPropertyReport report = builder.setReportId(reportId).build();

		assertNotNull(report);

		// precondition: report id is null
		ContractException contractException = assertThrows(ContractException.class, () -> GlobalPropertyReport.builder().build());
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "includePropertyId", args = {GlobalPropertyId.class})
	public void testIncludePropertyId () {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		ReportId reportId = new SimpleReportId(1001);
		GlobalPropertyReport report = builder.setReportId(reportId).build();

		// precondition test: if the property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.includePropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		GlobalPropertyId goodGlobalPropertyId = new SimpleGlobalPropertyId(15);
		builder.includePropertyId(goodGlobalPropertyId);
		assertNotNull(report);

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "includeAllExtantPropertyIds", args = {boolean.class})
	public void testIncludeAllExtantPropertyIds () {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		ReportId reportId = new SimpleReportId(1003);
		GlobalPropertyReport report = builder.setReportId(reportId).build();

		builder.includeAllExtantPropertyIds(true);
		assertNotNull(report);

	}

	@Test
	@UnitTestMethod(target =  GlobalPropertyReport.Builder.class, name = "includeNewPropertyIds", args = {boolean.class})
	public void testIncludeNewPropertyIds() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		ReportId reportId = new SimpleReportId(1002);
		GlobalPropertyReport report = builder.setReportId(reportId).build();

		builder.includeNewPropertyIds(true);
		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "excludePropertyId", args = {GlobalPropertyId.class})
	public void testExcludePropertyId () {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		ReportId reportId = new SimpleReportId(1004);
		GlobalPropertyReport report = builder.setReportId(reportId).build();

		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId(33);
		builder.excludePropertyId(globalPropertyId);
		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "setReportId", args = {ReportId.class})
	public void testSetReportId () {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();

		// precondition test: if the report id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setReportId(null));
		assertEquals(ReportError.NULL_REPORT_ID, contractException.getErrorType());

		ReportId reportId = new SimpleReportId(15);
		builder.setReportId(reportId);
		assertNotNull(reportId);
	}



	private static final ReportId REPORT_ID = new SimpleReportId("global property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("time").add("property").add("value").build();
}
