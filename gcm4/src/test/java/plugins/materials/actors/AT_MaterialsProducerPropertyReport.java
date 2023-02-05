package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerConstructionData;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public final class AT_MaterialsProducerPropertyReport {

	private ReportItem getReportItemFromPropertyId(ActorContext agentContext, MaterialsProducerId materialsProducerId,
			MaterialsProducerPropertyId materialsProducerPropertyId) {

		MaterialsDataManager materialsDataManager = agentContext.getDataManager(MaterialsDataManager.class);
		Object propertyValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId,
				materialsProducerPropertyId);

		ReportItem reportItem = getReportItem(//
				agentContext.getTime(), //
				materialsProducerId, //
				materialsProducerPropertyId, //
				propertyValue //
		);//

		return reportItem;
	}

	@Test
	@UnitTestConstructor(target = MaterialsProducerPropertyReport.class, args = { ReportLabel.class })
	public void testConstructor() {
		MaterialsProducerPropertyReport report = new MaterialsProducerPropertyReport(REPORT_LABEL);

		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReport.class, name = "init", args = {
			ActorContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		
		/*
		 * The test fails -- only the day value is wrong. 
		 */

		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;
		MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
						.values()) {
					expectedReportItems.put(
							getReportItemFromPropertyId(c, testMaterialsProducerId, testMaterialsProducerPropertyId),
							1);
				}
			}
		}));

		// add a new materials producer at time 30
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(30, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();

			builder.setMaterialsProducerId(newMaterialsProducerId);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.getPropertiesWithoutDefaultValues()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
			}

			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();
			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.values()) {
				expectedReportItems.put(
						getReportItemFromPropertyId(c, newMaterialsProducerId, testMaterialsProducerPropertyId), 1);
			}
		}));

		for (int i = 0; i < 5; i++) {

			// set a property value
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				List<MaterialsProducerId> materialsProducerIds = new ArrayList<>(
						materialsDataManager.getMaterialsProducerIds());
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = materialsProducerIds
						.get(randomGenerator.nextInt(materialsProducerIds.size()));
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId
						.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId,
						testMaterialsProducerPropertyId, propertyValue);
				expectedReportItems
						.put(getReportItemFromPropertyId(c, materialsProducerId, testMaterialsProducerPropertyId), 1);

			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		List<Plugin> pluginsToAdd = MaterialsActionSupport.setUpPluginsForTest(8759226038479000135L);
		pluginsToAdd.add(testPlugin);
		pluginsToAdd.add(ReportsTestPluginFactory.getPluginFromReport(new MaterialsProducerPropertyReport(REPORT_LABEL)::init));

		TestSimulation.executeSimulation(pluginsToAdd, outputConsumer);
		
		assertTrue(outputConsumer.isComplete());
		Map<ReportItem, Integer> acutualReportItems = outputConsumer.getOutputItems(ReportItem.class);
		assertEquals(expectedReportItems, acutualReportItems);
	}

	private static ReportItem getReportItem(Object... values) {
		Builder builder = ReportItem.builder().setReportLabel(REPORT_LABEL).setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("report");

	private static final ReportHeader REPORT_HEADER = getReportHeader();

	private static ReportHeader getReportHeader() {
		ReportHeader.Builder builder = ReportHeader.builder()//
				.add("time")//
				.add("materials_producer")//
				.add("property")//
				.add("value");//
		return builder.build();
	}

}