package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestBatchConstructionInfo;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.SimpleReportId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchStatusReport.class)

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

		List<Object> elements =  new ArrayList<>();
		
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
	@UnitTestMethod(name = "init", args = {ActorContext.class})
	public void testInit() {

		Set<ReportItem> expectedReportItems = new LinkedHashSet<>();

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
					BatchConstructionInfo batchConstructionInfo = TestBatchConstructionInfo.getBatchConstructionInfo(testMaterialsProducerId, materialId, amount, randomGenerator);
					BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
					expectedReportItems.add(getReportItemFromBatch(c, batchId));
				}

			}));

			// transfer material between batches
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {

				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);

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
							expectedReportItems.add(getReportItemFromBatch(c, batchId1));
							expectedReportItems.add(getReportItemFromBatch(c, batchId2));
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
					expectedReportItems.add(getReportItemFromBatch(c, batchId));
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
					TestBatchPropertyId propertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId, randomGenerator);
					Object value = propertyId.getRandomPropertyValue(randomGenerator);
					materialsDataManager.setBatchPropertyValue(batchId, propertyId, value);
					expectedReportItems.add(getReportItemFromBatch(c, batchId));
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
						expectedReportItems.add(getReportItemFromBatch(c, batchId));
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
							expectedReportItems.add(getReportItemFromBatch(c, batchId));
						}
					}
				}
			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		//Set<ReportItem> actualReportItems = MaterialsActionSupport.testConsumers(8914112012010329946L, testPlugin, new BatchStatusReport(REPORT_ID)::init);
		Set<ReportItem> actualReportItems = MaterialsActionSupport.testConsumers(2819236410498978100L, testPlugin, new BatchStatusReport(REPORT_ID)::init);
		

		assertEquals(expectedReportItems, actualReportItems);
	}

	private static ReportItem getReportItem(List<Object> values) {
		Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("report");

	private static final ReportHeader REPORT_HEADER = getReportHeader();

	private static ReportHeader getReportHeader() {

		ReportHeader.Builder builder = ReportHeader	.builder()//
													.add("time")//
													.add("batch")//
													.add("materials_producer")//
													.add("stage")//
													.add("material")//
													.add("amount");//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				builder.add(testMaterialId + "." + testBatchPropertyId);
			}
		}

		return builder.build();

	}

}
