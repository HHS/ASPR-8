package gov.hhs.aspr.ms.gcm.plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.MaterialsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.MaterialsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestBatchConstructionInfo;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestBatchPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.testsupport.TestMaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem.Builder;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public final class AT_BatchStatusReport {

	private ReportItem getReportItemFromBatch(ActorContext agentContext, BatchId batchId) {
		MaterialsDataManager materialsDataManager = agentContext.getDataManager(MaterialsDataManager.class);
		MaterialsProducerId batchProducer = materialsDataManager.getBatchProducer(batchId);
		MaterialId batchMaterialId = materialsDataManager.getBatchMaterial(batchId);
		double amount = materialsDataManager.getBatchAmount(batchId);
		Optional<StageId> optionalStageId = materialsDataManager.getBatchStageId(batchId);
		String stageString = "";
		if (optionalStageId.isPresent()) {
			stageString = optionalStageId.get().toString();
		}

		List<Object> elements = new ArrayList<>();

		elements.add(agentContext.getTime());
		elements.add(batchId);
		elements.add(batchProducer);
		elements.add(stageString);
		elements.add(batchMaterialId);
		elements.add(amount);

		for (MaterialId materialId : materialsDataManager.getMaterialIds()) {
			boolean matchingMaterial = batchMaterialId.equals(materialId);
			Set<BatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				if (matchingMaterial) {
					elements.add(materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId));
				} else {
					elements.add("");
				}
			}
		}

		ReportItem reportItem = getReportItem(elements);

		return reportItem;
	}

	@Test
	@UnitTestConstructor(target = BatchStatusReport.class, args = { BatchStatusReportPluginData.class })
	public void testConstructor() {
		BatchStatusReport report = new BatchStatusReport(BatchStatusReportPluginData.builder().setReportLabel(REPORT_LABEL).build());

		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = BatchStatusReport.class, name = "init", args = { ReportContext.class }, tags = {UnitTag.INCOMPLETE })
	public void testInit() {

		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			// add a few batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int i = 0; i < 20; i++) {
					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo
							.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					expectedReportItems.put(getReportItemFromBatch(c, batchId), 1);
				}

			}));

			// transfer material between batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batches = materialsDataManager
							.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);

					if (batches.size() > 1) {
						for (int i = 0; i < batches.size(); i++) {
							int index1 = randomGenerator.nextInt(batches.size());
							int index2 = randomGenerator.nextInt(batches.size() - 1);
							if (index2 >= index1) {
								index2++;
							}
							BatchId batchId1 = batches.get(index1);
							BatchId batchId2 = batches.get(index2);
							double portion = randomGenerator.nextDouble();
							double amount = materialsDataManager.getBatchAmount(batchId1);
							double transferAmount = amount *= portion;
							materialsDataManager.transferMaterialBetweenBatches(batchId1, batchId2, transferAmount);
							expectedReportItems.put(getReportItemFromBatch(c, batchId1), 1);
							expectedReportItems.put(getReportItemFromBatch(c, batchId2), 1);
						}
					}
				}
			}));

			// destroy some batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				Random random = new Random(randomGenerator.nextLong());
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Collections.shuffle(inventoryBatches, random);
				int destructionCount = inventoryBatches.size() / 5;
				for (int i = 0; i < destructionCount; i++) {
					BatchId batchId = inventoryBatches.get(i);
					expectedReportItems.put(getReportItemFromBatch(c, batchId), 1);
					materialsDataManager.removeBatch(batchId);
				}
			}));

			// set some batch property values
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);

				for (BatchId batchId : inventoryBatches) {
					TestMaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					TestBatchPropertyId propertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId,
							randomGenerator);
					Object value = propertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setBatchPropertyValue(batchId, propertyId, value);
					expectedReportItems.put(getReportItemFromBatch(c, batchId), 1);
				}
			}));

			// put some of the batches on stages
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stageIds = new ArrayList<>();
				for (int i = 0; i < 3; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					stageIds.add(stageId);
				}

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);
						expectedReportItems.put(getReportItemFromBatch(c, batchId), 1);
					}
				}

			}));

			// take some of the batches off of stages
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stageIds = materialsDataManager.getStages(testMaterialsProducerId);

				for (StageId stageId : stageIds) {
					List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							materialsDataManager.moveBatchToInventory(batchId);
							expectedReportItems.put(getReportItemFromBatch(c, batchId), 1);
						}
					}
				}
			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = MaterialsTestPluginFactory//
				.factory(0, 0, 0, 2819236410498978100L, testPluginData)
				.setBatchStatusReportPluginData(BatchStatusReportPluginData.builder().setReportLabel(REPORT_LABEL).build());

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();
		
		assertEquals(expectedReportItems, testOutputConsumer.getOutputItemMap(ReportItem.class));
	}

	@Test
	@UnitTestMethod(target = BatchStatusReport.class, name = "init", args = { ReportContext.class })
	public void testStateFinalization() {
		// Test with producing simulation

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			// add a few batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (int i = 0; i < 20; i++) {
					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo
							.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
					materialsDataManager.addBatch(batchConstructionInfo);
				}
			}));

			// transfer material between batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batches = materialsDataManager
							.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);

					if (batches.size() > 1) {
						for (int i = 0; i < batches.size(); i++) {
							int index1 = randomGenerator.nextInt(batches.size());
							int index2 = randomGenerator.nextInt(batches.size() - 1);
							if (index2 >= index1) {
								index2++;
							}
							BatchId batchId1 = batches.get(index1);
							BatchId batchId2 = batches.get(index2);
							double portion = randomGenerator.nextDouble();
							double amount = materialsDataManager.getBatchAmount(batchId1);
							double transferAmount = amount *= portion;
							materialsDataManager.transferMaterialBetweenBatches(batchId1, batchId2, transferAmount);
						}
					}
				}
			}));

			// destroy some batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				Random random = new Random(randomGenerator.nextLong());
				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				Collections.shuffle(inventoryBatches, random);
				int destructionCount = inventoryBatches.size() / 5;
				for (int i = 0; i < destructionCount; i++) {
					BatchId batchId = inventoryBatches.get(i);
					materialsDataManager.removeBatch(batchId);
				}
			}));

			// set some batch property values
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);

				for (BatchId batchId : inventoryBatches) {
					TestMaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
					TestBatchPropertyId propertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId,
							randomGenerator);
					Object value = propertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setBatchPropertyValue(batchId, propertyId, value);
				}
			}));

			// put some of the batches on stages
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stageIds = new ArrayList<>();
				for (int i = 0; i < 3; i++) {
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					stageIds.add(stageId);
				}

				List<BatchId> inventoryBatches = materialsDataManager.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
						materialsDataManager.moveBatchToStage(batchId, stageId);
					}
				}

			}));

			// take some of the batches off of stages
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				List<StageId> stageIds = materialsDataManager.getStages(testMaterialsProducerId);

				for (StageId stageId : stageIds) {
					List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							materialsDataManager.moveBatchToInventory(batchId);
						}
					}
				}
			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();

		BatchStatusReportPluginData batchStatusReportPluginData = BatchStatusReportPluginData.builder()
				.setReportLabel(REPORT_LABEL)
				.build();

		Factory factory = MaterialsTestPluginFactory//
				.factory(0, 0, 0, 2819236410498978100L, testPluginData)
				.setBatchStatusReportPluginData(batchStatusReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		// show that the plugin data persists after simulation
		Map<BatchStatusReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(BatchStatusReportPluginData.class);
		assertEquals(1, outputItems.size());
		BatchStatusReportPluginData batchStatusReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(batchStatusReportPluginData, batchStatusReportPluginData2);

		// Test without producing simulation
		testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		outputItems = testOutputConsumer.getOutputItemMap(BatchStatusReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static ReportItem getReportItem(List<Object> values) {
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
				.add("batch")//
				.add("materials_producer")//
				.add("stage")//
				.add("material")//
				.add("amount");//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId
					.getTestBatchPropertyIds(testMaterialId)) {
				builder.add(testMaterialId + "." + testBatchPropertyId);
			}
		}

		return builder.build();

	}

}
