package gov.hhs.aspr.ms.gcm.plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.MaterialsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.MaterialsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem.Builder;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

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
	@UnitTestConstructor(target = MaterialsProducerPropertyReport.class, args = { MaterialsProducerPropertyReportPluginData.class })
	public void testConstructor() {
		MaterialsProducerPropertyReport report = new MaterialsProducerPropertyReport(MaterialsProducerPropertyReportPluginData.builder().setReportLabel(REPORT_LABEL).build());
		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReport.class, name = "init", args = {ReportContext.class }, tags = { UnitTag.INCOMPLETE })
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

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8759226038479000135L, testPluginData);
		factory.setMaterialsProducerPropertyReportPluginData(MaterialsProducerPropertyReportPluginData.builder().setReportLabel(REPORT_LABEL).build());
		
		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();
		
		Map<ReportItem, Integer> acutualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertEquals(expectedReportItems, acutualReportItems);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReport.class, name = "init", args = {ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation state

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;
		MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
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
			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();

		MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = MaterialsProducerPropertyReportPluginData
				.builder()
				.setReportLabel(REPORT_LABEL)
				.build();

		Factory factory = MaterialsTestPluginFactory.factory(0, 0, 0, 8759226038479000135L, testPluginData);
		factory.setMaterialsProducerPropertyReportPluginData(materialsProducerPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(50)//
				.build()//
				.execute();

		Map<MaterialsProducerPropertyReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(MaterialsProducerPropertyReportPluginData.class);
		assertEquals(1, outputItems.size());
		MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(materialsProducerPropertyReportPluginData, materialsProducerPropertyReportPluginData2);

		// Test without producing simulation state

		testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(50)//
				.build()//
				.execute();

		outputItems = testOutputConsumer.getOutputItemMap(MaterialsProducerPropertyReportPluginData.class);
		assertEquals(0, outputItems.size());
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