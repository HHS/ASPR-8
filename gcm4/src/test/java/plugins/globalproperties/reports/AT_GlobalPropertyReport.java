package plugins.globalproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.GlobalPropertyInitialization;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory.Factory;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_GlobalPropertyReport {

	@Test
	@UnitTestConstructor(target = GlobalPropertyReport.class, args = { GlobalPropertyReportPluginData.class }, tags = {})
	public void testConstructor() {
		// construction is covered by the other tests

		// precondition test: if the GlobalPropertyReportPluginData is null
		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyReport(null));
		assertEquals(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_Content() {

		/*
		 * We will add one actor and the global property report to the engine.
		 * We will define a few global properties and the actor will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality.
		 */

		GlobalPropertyReportPluginData globalPropertyReportPluginData = GlobalPropertyReportPluginData	.builder()//
																										.setReportLabel(REPORT_LABEL)//
																										.setDefaultInclusion(true)//
																										.build();

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

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		GlobalPropertyId globalPropertyId_4 = new SimpleGlobalPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		GlobalPropertyId globalPropertyId_5 = new SimpleGlobalPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

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

		Factory factory = GlobalPropertiesTestPluginFactory	.factory(testPluginData)//
															.setGlobalPropertiesPluginData(globalPropertiesPluginData)//
															.setGlobalPropertyReportPluginData(globalPropertyReportPluginData);//

		TestOutputConsumer outputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

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
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { GlobalPropertyId.class })
	public void testInit_IncludeProperty() {
		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly included
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several global property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				if (testGlobalPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
					Object globalPropertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
					globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, globalPropertyValue);
				}
			}

		}));

		GlobalPropertyId unknownGlobalPropertyId = TestGlobalPropertyId.getUnknownGlobalPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization//
																									.builder()//
																									.setGlobalPropertyId(unknownGlobalPropertyId)//
																									.setPropertyDefinition(propertyDefinition).build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

			globalPropertiesDataManager.setGlobalPropertyValue(unknownGlobalPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setDefaultInclusion(false);
		builder.includeGlobalProperty(testGlobalPropertyId);
		builder.includeGlobalProperty(unknownGlobalPropertyId);
		GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		GlobalPropertiesTestPluginFactory.Factory factory = //
				GlobalPropertiesTestPluginFactory//
													.factory(testPluginData)//
													.setGlobalPropertyReportPluginData(globalPropertyReportPluginData);

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(4059891083116386869L).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		// tell the builder to include a specific global property id
		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.addPlugin(stochasticsPlugin)//
																.build()//
																.execute();

		// show that our report items include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(1));
		}
		assertTrue(outputPropertyStrings.contains(testGlobalPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownGlobalPropertyId.toString()));
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { GlobalPropertyId.class })
	public void testInit_DefaultInclusion() {

		// group the properties into explicitly included, explicitly excluded,
		// and those that are not specified
		GlobalPropertyId includedPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;
		GlobalPropertyId excludedPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE;
		Set<GlobalPropertyId> middlePropertyIds = new LinkedHashSet<>();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			middlePropertyIds.add(testGlobalPropertyId);
		}
		middlePropertyIds.remove(includedPropertyId);
		middlePropertyIds.remove(excludedPropertyId);

		// create an enum to represent setting the default inclusion policy
		enum DefaultInclusionPolicy {
			TRUE, FALSE, UNSPECIFIED
		}
		;

		// loop over the three policies
		for (DefaultInclusionPolicy defaultInclusionPolicy : DefaultInclusionPolicy.values()) {

			// build the report plugin data
			GlobalPropertyReportPluginData.Builder reportBuilder = GlobalPropertyReportPluginData.builder();
			reportBuilder.setReportLabel(new SimpleReportLabel("report label"));
			switch (defaultInclusionPolicy) {
			case FALSE:
				reportBuilder.setDefaultInclusion(false);
				break;
			case TRUE:
				reportBuilder.setDefaultInclusion(true);
				break;
			default:
				// do nothing
			}

			reportBuilder.includeGlobalProperty(includedPropertyId);
			reportBuilder.excludeGlobalProperty(excludedPropertyId);
			GlobalPropertyReportPluginData globalPropertyReportPluginData = reportBuilder.build();

			// build the global plugin using the report plugin data and the
			// standard global plugin data build
			Plugin globalPropertiesPlugin = GlobalPropertiesPlugin	.builder()//
																	.setGlobalPropertiesPluginData(GlobalPropertiesTestPluginFactory.getStandardGlobalPropertiesPluginData())//
																	.setGlobalPropertyReportPluginData(globalPropertyReportPluginData)//
																	.getGlobalPropertiesPlugin();//

			// create an output consumer to gather the report items
			TestOutputConsumer testOutputConsumer = new TestOutputConsumer();

			// execute the simulation
			Simulation	.builder()//
						.addPlugin(globalPropertiesPlugin)//
						.setOutputConsumer(testOutputConsumer).build()//
						.execute();//

			// gather from the report items the property ids that were actually
			// included in the report
			Set<GlobalPropertyId> actualPropertyIds = new LinkedHashSet<>();
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
			for (ReportItem reportItem : outputItems.keySet()) {
				Integer count = outputItems.get(reportItem);
				assertEquals(1, count);
				TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.valueOf(reportItem.getValue(1));
				actualPropertyIds.add(testGlobalPropertyId);
			}

			// build the expected property ids based on the policy
			Set<GlobalPropertyId> expectedPropertyIds = new LinkedHashSet<>();
			expectedPropertyIds.add(includedPropertyId);

			switch (defaultInclusionPolicy) {
			case FALSE:
				// only the single included property
				break;
			default:
				expectedPropertyIds.addAll(middlePropertyIds);
				break;
			}

			// show that the property id sets are equals
			assertEquals(expectedPropertyIds, actualPropertyIds);

		}

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { GlobalPropertyId.class })
	public void testInit_ReportHeader() {
		/*
		 * This test shows that the report produces report items with the
		 * correct header
		 */

		GlobalPropertyReportPluginData globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData	.builder()//
												.setReportLabel(new SimpleReportLabel("report label"))//
												.build();

		Plugin globalPropertiesPlugin = GlobalPropertiesPlugin	.builder()//
																.setGlobalPropertiesPluginData(GlobalPropertiesTestPluginFactory.getStandardGlobalPropertiesPluginData())
																.setGlobalPropertyReportPluginData(globalPropertyReportPluginData).getGlobalPropertiesPlugin();

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();

		Simulation	.builder()//
					.setOutputConsumer(testOutputConsumer)//
					.addPlugin(globalPropertiesPlugin)//
					.build()//
					.execute();

		// show that the report labels are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(REPORT_HEADER, reportItem.getReportHeader());
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { GlobalPropertyId.class })
	public void testInit_ExcludeProperty() {
		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly included
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several global property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				if (testGlobalPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
					Object globalPropertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
					globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, globalPropertyValue);
				}
			}

		}));

		GlobalPropertyId unknownGlobalPropertyId = TestGlobalPropertyId.getUnknownGlobalPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization//
																									.builder()//
																									.setGlobalPropertyId(unknownGlobalPropertyId)//
																									.setPropertyDefinition(propertyDefinition).build();
			globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

			globalPropertiesDataManager.setGlobalPropertyValue(unknownGlobalPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setDefaultInclusion(true);
		builder.excludeGlobalProperty(testGlobalPropertyId);
		builder.excludeGlobalProperty(unknownGlobalPropertyId);
		GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		GlobalPropertiesTestPluginFactory.Factory factory = //
				GlobalPropertiesTestPluginFactory//
													.factory(testPluginData)//
													.setGlobalPropertyReportPluginData(globalPropertyReportPluginData);

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(4059891083116386869L).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		// tell the builder to include a specific global property id
		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.addPlugin(stochasticsPlugin)//
																.build()//
																.execute();

		// show that our report items exclude the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(1));
		}
		assertFalse(outputPropertyStrings.contains(testGlobalPropertyId.toString()));
		assertFalse(outputPropertyStrings.contains(unknownGlobalPropertyId.toString()));

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { GlobalPropertyId.class })
	public void testInit_ReportLabel() {
		/*
		 * This test shows that the report produces report items with the
		 * correct header
		 */

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		GlobalPropertyReportPluginData globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.build();

		Plugin globalPropertiesPlugin = GlobalPropertiesPlugin	.builder()//
																.setGlobalPropertiesPluginData(GlobalPropertiesTestPluginFactory.getStandardGlobalPropertiesPluginData())
																.setGlobalPropertyReportPluginData(globalPropertyReportPluginData).getGlobalPropertiesPlugin();

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();

		Simulation	.builder()//
					.setOutputConsumer(testOutputConsumer)//
					.addPlugin(globalPropertiesPlugin)//
					.build()//
					.execute();

		// show that the report labels are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportLabel, reportItem.getReportLabel());
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_StateRecording() {

		/*
		 * We will use the standard global properties plugin provided in the
		 * factory.
		 * 
		 * We will add a new property definition
		 * 
		 * We will set several property values
		 * 
		 * We will extract the plugin
		 */

		GlobalPropertyReportPluginData globalPropertyReportPluginData = GlobalPropertyReportPluginData	.builder()//
																										.setReportLabel(REPORT_LABEL)//
																										.setDefaultInclusion(true)//
																										.build();

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

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		GlobalPropertyId globalPropertyId_4 = new SimpleGlobalPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		GlobalPropertyId globalPropertyId_5 = new SimpleGlobalPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

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

		Factory factory = GlobalPropertiesTestPluginFactory	.factory(testPluginData)//
															.setGlobalPropertiesPluginData(globalPropertiesPluginData)//
															.setGlobalPropertyReportPluginData(globalPropertyReportPluginData);//

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		assertEquals(expectedReportItems, testOutputConsumer.getOutputItems(ReportItem.class));

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {

		/*
		 * We will add one actor and the global property report to the engine.
		 * We will define a few global properties and the actor will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality.
		 */

		GlobalPropertyReportPluginData globalPropertyReportPluginData = GlobalPropertyReportPluginData	.builder()//
																										.setReportLabel(REPORT_LABEL)//
																										.setDefaultInclusion(true)//
																										.build();

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

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		GlobalPropertyId globalPropertyId_4 = new SimpleGlobalPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		GlobalPropertyId globalPropertyId_5 = new SimpleGlobalPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

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

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */

		Factory factory = GlobalPropertiesTestPluginFactory	.factory(testPluginData)//
															.setGlobalPropertiesPluginData(globalPropertiesPluginData)//
															.setGlobalPropertyReportPluginData(globalPropertyReportPluginData);//

		TestOutputConsumer outputConsumer = TestSimulation	.builder()//
															.addPlugins(factory.getPlugins())//
															.setProduceSimulationStateOnHalt(true)//
															.setSimulationHaltTime(20)//
															.build()//
															.execute();

		// show that the GlobalPropertyReportPluginData produced by the
		// simulation is equal to the one used to form the report
		Map<GlobalPropertyReportPluginData, Integer> outputItems = outputConsumer.getOutputItems(GlobalPropertyReportPluginData.class);
		assertEquals(1,outputItems.size());
		GlobalPropertyReportPluginData globalPropertyReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(globalPropertyReportPluginData, globalPropertyReportPluginData2);
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("global property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("time").add("property").add("value").build();
}
