package plugins.materials.reports;

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

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsTestPluginFactory;
import plugins.materials.testsupport.TestBatchConstructionInfo;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

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
	@UnitTestConstructor(target = BatchStatusReport.class, args = { ReportLabel.class })
	public void testConstructor() {
		BatchStatusReport report = new BatchStatusReport(BatchStatusReportPluginData.builder().setReportLabel(REPORT_LABEL).build());

		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = BatchStatusReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
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

		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		List<Plugin> pluginsToAdd = MaterialsTestPluginFactory.factory(0, 0, 0, 2819236410498978100L, testPluginData)
				.getPlugins();
		pluginsToAdd.add(ReportsTestPluginFactory.getPluginFromReport(new BatchStatusReport(BatchStatusReportPluginData.builder().setReportLabel(REPORT_LABEL).build())::init));

		TestSimulation.executeSimulation(pluginsToAdd, outputConsumer);

		
		assertEquals(expectedReportItems, outputConsumer.getOutputItems(ReportItem.class));
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
