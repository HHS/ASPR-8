package plugins.globalproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.GlobalPropertyInitialization;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_GlobalPropertyReport {

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "builder", args = {})
	public void testBuilder() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testInit() {

		/*
		 * We will add one actor and the global property report to the engine.
		 * We will define a few global properties and the actor will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality.
		 */


		// add the global property definitions
		GlobalPropertiesPluginData.Builder initialDatabuilder = GlobalPropertiesPluginData.builder();

		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3)
				.build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		GlobalPropertiesPluginData globalPropertiesPluginData = initialDatabuilder.build();

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		GlobalPropertyId globalPropertyId_4 = new SimpleGlobalPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class)
				.setDefaultValue(true).build();

		GlobalPropertyId globalPropertyId_5 = new SimpleGlobalPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class)
				.setDefaultValue(199.16).build();

		GlobalPropertyReport globalPropertyReport = GlobalPropertyReport.builder()//
				.setReportLabel(REPORT_LABEL)//
				.includeAllExtantPropertyIds(true)//
				.includeNewPropertyIds(true)//
				.build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various global properties at
		// various times

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			GlobalPropertiesDataManager globalPropertiesDataManager = c
					.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_1, 67);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			GlobalPropertiesDataManager globalPropertiesDataManager = c
					.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 88.88);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c
					.getDataManager(GlobalPropertiesDataManager.class);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_1, 100);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 3.45);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder()
					.setGlobalPropertyId(globalPropertyId_4).setPropertyDefinition(propertyDefinition_4)
					.build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c
					.getDataManager(GlobalPropertiesDataManager.class);

			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, false);
			// note the duplicated value
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 99.7);
			// and now a third setting of the same property to a new value
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_2, 100.0);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId_3, true);
			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder()
					.setGlobalPropertyId(globalPropertyId_5).setPropertyDefinition(propertyDefinition_5)
					.build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

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

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(testPluginData).setGlobalPropertiesPluginData(globalPropertiesPluginData).getPlugins();
		plugins.add(ReportsTestPluginFactory.getPluginFromReport(globalPropertyReport::init));
		TestSimulation.executeSimulation(plugins, outputConsumer);

		assertEquals(expectedReportItems, outputConsumer.getOutputItems(ReportItem.class));

	}

	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);
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
		ReportLabel reportLabel = new SimpleReportLabel(1000);
		GlobalPropertyReport report = builder.setReportLabel(reportLabel).build();

		assertNotNull(report);

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertyReport.builder().build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "includePropertyId", args = {
			GlobalPropertyId.class })
	public void testIncludePropertyId() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();

		// precondition test: if the property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.includePropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "includeAllExtantPropertyIds", args = {
			boolean.class })
	public void testIncludeAllExtantPropertyIds() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "includeNewPropertyIds", args = {
			boolean.class })
	public void testIncludeNewPropertyIds() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "excludePropertyId", args = {
			GlobalPropertyId.class })
	public void testExcludePropertyId() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();

		// precondition test: if the property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> builder.excludePropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {
		GlobalPropertyReport.Builder builder = GlobalPropertyReport.builder();

		// precondition test: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setReportLabel(null));
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("global property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("time").add("property").add("value")
			.build();
}
