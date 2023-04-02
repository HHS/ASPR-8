package plugins.regions.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDefinitionInitialization;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionPropertyReport {

	@Test
	@UnitTestConstructor(target = RegionPropertyReport.class, args = { RegionPropertyReportPluginData.class }, tags = {})
	public void testConstructor() {
		// construction is covered by the other tests

		// precondition test: if the RegionPropertyReportPluginData is null
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionPropertyReport(null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_Content() {

		/*
		 * We will add one actor and the region property report to the engine.
		 * We will define a few region properties and the actor will alter
		 * various region properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality.
		 */

		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData	.builder()//
												.setReportLabel(REPORT_LABEL)//
												.setDefaultInclusion(true)//
												.build();

		// add the global property definitions

		RegionsPluginData.Builder initialDatabuilder = RegionsPluginData.builder();

		RegionId regionA = new SimpleRegionId("Region_A");
		initialDatabuilder.addRegion(regionA);
		RegionId regionB = new SimpleRegionId("Region_B");
		initialDatabuilder.addRegion(regionB);
		RegionId regionC = new SimpleRegionId("Region_C");
		initialDatabuilder.addRegion(regionC);

		RegionPropertyId regionPropertyId_1 = new SimpleRegionPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_1, propertyDefinition);

		RegionPropertyId regionPropertyId_2 = new SimpleRegionPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_2, propertyDefinition);

		RegionPropertyId regionPropertyId_3 = new SimpleRegionPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_3, propertyDefinition);

		RegionsPluginData regionsPluginData = initialDatabuilder.build();

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		RegionPropertyId regionPropertyId_4 = new SimpleRegionPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		RegionPropertyId regionPropertyId_5 = new SimpleRegionPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various region properties at
		// various times

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, regionPropertyId_1, 67);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 88.88);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, regionPropertyId_1, 100);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 3.45);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, true);
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization	.builder().setRegionPropertyId(regionPropertyId_4)
																																	.setPropertyDefinition(propertyDefinition_4).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, false);
			// note the duplicated value
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 99.7);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 99.7);
			// and now a third setting of the same property to a new value
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 100.0);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, true);
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization	.builder().setRegionPropertyId(regionPropertyId_5)
																																	.setPropertyDefinition(propertyDefinition_5).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		expectedOutputConsumer.accept(getReportItem(0.0, regionA, regionPropertyId_1, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, regionPropertyId_2, 6.78));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, regionPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, regionPropertyId_1, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, regionPropertyId_2, 6.78));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, regionPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, regionPropertyId_1, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, regionPropertyId_2, 6.78));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, regionPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, regionPropertyId_1, 67));
		expectedOutputConsumer.accept(getReportItem(1.0, regionB, regionPropertyId_2, 88.88));
		expectedOutputConsumer.accept(getReportItem(1.0, regionC, regionPropertyId_3, false));
		expectedOutputConsumer.accept(getReportItem(2.0, regionA, regionPropertyId_1, 100));
		expectedOutputConsumer.accept(getReportItem(2.0, regionB, regionPropertyId_2, 3.45));
		expectedOutputConsumer.accept(getReportItem(2.0, regionC, regionPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(2.0, regionA, regionPropertyId_4, true));
		expectedOutputConsumer.accept(getReportItem(2.0, regionB, regionPropertyId_4, true));
		expectedOutputConsumer.accept(getReportItem(2.0, regionC, regionPropertyId_4, true));
		expectedOutputConsumer.accept(getReportItem(3.0, regionC, regionPropertyId_3, false));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, regionPropertyId_2, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, regionPropertyId_2, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, regionPropertyId_2, 100.0));
		expectedOutputConsumer.accept(getReportItem(3.0, regionC, regionPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(3.0, regionA, regionPropertyId_5, 199.16));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, regionPropertyId_5, 199.16));
		expectedOutputConsumer.accept(getReportItem(3.0, regionC, regionPropertyId_5, 199.16));

		Factory factory = RegionsTestPluginFactory	.factory(0, 3558607823596502222L, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
													.setRegionsPluginData(regionsPluginData)//
													.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItems(ReportItem.class);

		assertEquals(expectedReportItems, actualReportItems);

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
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_IncludeProperty() {
		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly included
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several region property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
					Object regionPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, testRegionPropertyId, regionPropertyValue);
				}
			}
		}));

		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization//
																																	.builder()//
																																	.setRegionPropertyId(unknownRegionPropertyId)//
																																	.setPropertyDefinition(propertyDefinition).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, unknownRegionPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setDefaultInclusion(false);
		builder.includeRegionProperty(testRegionPropertyId);
		builder.includeRegionProperty(unknownRegionPropertyId);
		RegionPropertyReportPluginData regionPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = RegionsTestPluginFactory//
													.factory(0, 424190079221645034L, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
													.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		// tell the builder to include a specific region property id

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		// show that our report items include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(2));
		}
		assertTrue(outputPropertyStrings.contains(testRegionPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownRegionPropertyId.toString()));
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_DefaultInclusion() {

		// group the properties into explicitly included, explicitly excluded,
		// and those that are not specified
		RegionPropertyId includedPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;
		RegionPropertyId excludedPropertyId = TestRegionPropertyId.REGION_PROPERTY_6_DOUBLE_IMMUTABLE;
		Set<RegionPropertyId> middlePropertyIds = new LinkedHashSet<>();
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			middlePropertyIds.add(testRegionPropertyId);
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
			RegionPropertyReportPluginData.Builder reportBuilder = RegionPropertyReportPluginData.builder();
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

			reportBuilder.includeRegionProperty(includedPropertyId);
			reportBuilder.excludeRegionProperty(excludedPropertyId);
			RegionPropertyReportPluginData regionPropertyReportPluginData = reportBuilder.build();

			// build the region plugin using the report plugin data and the
			// standard region plugin data build
			Factory factory = RegionsTestPluginFactory//
														.factory(0, 6198510650051666838L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
														})//
														.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

			// execute the simulation
			TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																	.addPlugins(factory.getPlugins())//
																	.build()//
																	.execute();

			// gather from the report items the property ids that were actually
			// included in the report
			Set<TestRegionPropertyId> actualPropertyIds = new LinkedHashSet<>();
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
			for (ReportItem reportItem : outputItems.keySet()) {
				Integer count = outputItems.get(reportItem);
				assertEquals(1, count);
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.valueOf(reportItem.getValue(2));
				actualPropertyIds.add(testRegionPropertyId);
			}

			// build the expected property ids based on the policy
			Set<RegionPropertyId> expectedPropertyIds = new LinkedHashSet<>();
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
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ReportHeader() {
		/*
		 * This test shows that the report produces report items with the
		 * correct header
		 */

		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData	.builder()//
												.setReportLabel(new SimpleReportLabel("report label"))//
												.build();

		Factory factory = RegionsTestPluginFactory//
													.factory(0, 2608710368019720039L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
													})//
													.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
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
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ExcludeProperty() {
		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly included
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several region property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
				if (testRegionPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
					Object regionPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
					regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, testRegionPropertyId, regionPropertyValue);
				}
			}

		}));

		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization//
																																	.builder()//
																																	.setRegionPropertyId(unknownRegionPropertyId)//
																																	.setPropertyDefinition(propertyDefinition).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

			regionsDataManager.setRegionPropertyValue(TestRegionId.REGION_1, unknownRegionPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE;

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setDefaultInclusion(true);
		builder.excludeRegionProperty(testRegionPropertyId);
		builder.excludeRegionProperty(unknownRegionPropertyId);
		RegionPropertyReportPluginData regionPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();

		Factory factory = RegionsTestPluginFactory//
													.factory(0, 4660368916694613140L, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
													.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		// tell the builder to include a specific region property id

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		// show that our report items exclude the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertFalse(outputItems.isEmpty());

		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(1));
		}
		assertFalse(outputPropertyStrings.contains(testRegionPropertyId.toString()));
		assertFalse(outputPropertyStrings.contains(unknownRegionPropertyId.toString()));

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ReportLabel() {
		/*
		 * This test shows that the report produces report items with the
		 * correct header
		 */

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.build();

		Factory factory = RegionsTestPluginFactory//
													.factory(0, 6305425169353361697L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
													})//
													.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
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
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation state

		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData	.builder()//
						.setReportLabel(REPORT_LABEL)//
						.setDefaultInclusion(true)//
						.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE)
						.build();

		// add the global property definitions

		RegionsPluginData.Builder initialDatabuilder = RegionsPluginData.builder();

		RegionId regionA = new SimpleRegionId("Region_A");
		initialDatabuilder.addRegion(regionA);
		RegionId regionB = new SimpleRegionId("Region_B");
		initialDatabuilder.addRegion(regionB);
		RegionId regionC = new SimpleRegionId("Region_C");
		initialDatabuilder.addRegion(regionC);

		RegionPropertyId regionPropertyId_1 = new SimpleRegionPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_1, propertyDefinition);

		RegionPropertyId regionPropertyId_2 = new SimpleRegionPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_2, propertyDefinition);

		RegionPropertyId regionPropertyId_3 = new SimpleRegionPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineRegionProperty(regionPropertyId_3, propertyDefinition);

		RegionsPluginData regionsPluginData = initialDatabuilder.build();

		/*
		 * Define two more properties that are not included in the plugin data
		 * and will be added by an actor
		 */
		RegionPropertyId regionPropertyId_4 = new SimpleRegionPropertyId("id_4");
		PropertyDefinition propertyDefinition_4 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		RegionPropertyId regionPropertyId_5 = new SimpleRegionPropertyId("id_5");
		PropertyDefinition propertyDefinition_5 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(199.16).build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various region properties at
		// various times

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, regionPropertyId_1, 67);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 88.88);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, false);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, regionPropertyId_1, 100);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 3.45);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, true);
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization	.builder().setRegionPropertyId(regionPropertyId_4)
					.setPropertyDefinition(propertyDefinition_4).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);

			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, false);
			// note the duplicated value
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 99.7);
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 99.7);
			// and now a third setting of the same property to a new value
			regionsDataManager.setRegionPropertyValue(regionB, regionPropertyId_2, 100.0);
			regionsDataManager.setRegionPropertyValue(regionC, regionPropertyId_3, true);
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization	.builder().setRegionPropertyId(regionPropertyId_5)
					.setPropertyDefinition(propertyDefinition_5).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = RegionsTestPluginFactory	.factory(0, 3558607823596502222L, TimeTrackingPolicy.TRACK_TIME, testPluginData)//
				.setRegionsPluginData(regionsPluginData)//
				.setRegionPropertyReportPluginData(regionPropertyReportPluginData);//

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)
				.build()//
				.execute();

		Map<RegionPropertyReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItems(RegionPropertyReportPluginData.class);
		assertEquals(1, outputItems.size());
		RegionPropertyReportPluginData regionPropertyReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(regionPropertyReportPluginData, regionPropertyReportPluginData2);

		// Test without producing simulation output

		testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)
				.build()//
				.execute();

		outputItems = testOutputConsumer.getOutputItems(RegionPropertyReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("region property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Region").add("Property").add("Value").build();
}
