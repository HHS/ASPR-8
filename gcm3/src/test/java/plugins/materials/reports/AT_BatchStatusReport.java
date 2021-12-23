package plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.BatchContentShiftEvent;
import plugins.materials.events.mutation.BatchCreationEvent;
import plugins.materials.events.mutation.BatchPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.BatchRemovalRequestEvent;
import plugins.materials.events.mutation.MoveBatchToInventoryEvent;
import plugins.materials.events.mutation.MoveBatchToStageEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.datacontainers.StochasticsDataView;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = BatchStatusReport.class)

public final class AT_BatchStatusReport {

	private ReportItem getReportItemFromBatch(AgentContext agentContext, BatchId batchId) {
		MaterialsDataView materialsDataView = agentContext.getDataView(MaterialsDataView.class).get();
		MaterialsProducerId batchProducer = materialsDataView.getBatchProducer(batchId);
		MaterialId materialId = materialsDataView.getBatchMaterial(batchId);
		double amount = materialsDataView.getBatchAmount(batchId);
		Optional<StageId> optionalStageId = materialsDataView.getBatchStageId(batchId);
		String stageString = "";
		if (optionalStageId.isPresent()) {
			stageString = optionalStageId.get().toString();
		}

		ReportItem reportItem = getReportItem(agentContext.getTime(), //
				batchId, //
				batchProducer, //
				stageString, //
				materialId, //
				amount//
		);//

		return reportItem;
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		double actionTime = 0;

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			// add a few batches
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				for (int i = 0; i < 20; i++) {
					TestMaterialId materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					double amount = randomGenerator.nextDouble();
					c.resolveEvent(new BatchCreationEvent(materialId, amount));
					BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
					expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId));
				}

			}));

			// transfer material between batches
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {

				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					List<BatchId> batches = materialsDataView.getInventoryBatchesByMaterialId(testMaterialsProducerId, testMaterialId);

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
							double amount = materialsDataView.getBatchAmount(batchId1);
							double transferAmount = amount *= portion;
							c.resolveEvent(new BatchContentShiftEvent(batchId1, batchId2, transferAmount));
							expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId1));
							expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId2));
						}
					}
				}
			}));

			// destroy some batches
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				Random random = new Random(randomGenerator.nextLong());
				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				Collections.shuffle(inventoryBatches, random);
				int destructionCount = inventoryBatches.size() / 5;
				for (int i = 0; i < destructionCount; i++) {
					BatchId batchId = inventoryBatches.get(i);
					expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId));
					c.resolveEvent(new BatchRemovalRequestEvent(batchId));
				}
			}));

			// set some batch property values
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(testMaterialsProducerId);

				for (BatchId batchId : inventoryBatches) {
					TestMaterialId materialId = materialsDataView.getBatchMaterial(batchId);
					TestBatchPropertyId propertyId = TestBatchPropertyId.getRandomMutableBatchPropertyId(materialId, randomGenerator);
					Object value = propertyId.getRandomPropertyValue(randomGenerator);
					c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, propertyId, value));
					expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId));
				}
			}));

			// put some of the batches on stages
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<StageId> stageIds = new ArrayList<>();
				for (int i = 0; i < 3; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					stageIds.add(stageId);
				}

				List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : inventoryBatches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stageIds.get(randomGenerator.nextInt(stageIds.size()));
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
						expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId));
					}
				}

			}));

			// take some of the batches off of stages
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<StageId> stageIds = materialsDataView.getStages(testMaterialsProducerId);

				for (StageId stageId : stageIds) {
					List<BatchId> batches = materialsDataView.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							c.resolveEvent(new MoveBatchToInventoryEvent(batchId));
							expectedOutputConsumer.accept(getReportItemFromBatch(c, batchId));
						}
					}
				}
			}));

		}

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8914112012010329946L, actionPlugin, actualOutputConsumer, new BatchStatusReport()::init);

		assertEquals(expectedOutputConsumer, actualOutputConsumer);
	}

	private static ReportItem getReportItem(Object... values) {
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
													.add("Time")//
													.add("Batch")//
													.add("MaterialsProducer")//
													.add("Stage")//
													.add("Material")//
													.add("Amount");//

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
				builder.add(testMaterialId + "." + testBatchPropertyId);
			}
		}

		return builder.build();

	}

}
