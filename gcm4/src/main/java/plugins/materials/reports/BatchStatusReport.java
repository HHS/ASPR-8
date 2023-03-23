package plugins.materials.reports;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.ReportContext;
import nucleus.SimulationStateContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.BatchAdditionEvent;
import plugins.materials.events.BatchAmountUpdateEvent;
import plugins.materials.events.BatchImminentRemovalEvent;
import plugins.materials.events.BatchPropertyUpdateEvent;
import plugins.materials.events.StageMembershipAdditionEvent;
import plugins.materials.events.StageMembershipRemovalEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;

/**
 * A Report that displays the state of batches over time. The batch properties
 * included in this report are limited to those present during initialization.
 *
 *
 * Fields
 *
 * Time -- the time in days when batch state was updated
 *
 * Batch -- the batch identifier
 *
 * Stage -- the stage associated with the batch
 *
 * MaterialsProducer -- the materials producer of the owner of the batch
 * 
 * Offered -- the offered state of the batch
 * 
 * Material -- the material of the batch
 * 
 * Amount -- the amount of material in the batch
 * 
 * Material.PropertyId -- multiple columns for the batch properties selected for
 * the report
 * 
 *
 */
public final class BatchStatusReport {

	private static class BatchRecord {
		private double time;
		private BatchId batchId;
		private MaterialsProducerId materialsProducerId;
		private StageId stageId;
		private MaterialId materialId;
		private double amount;
		private Map<BatchPropertyId, Object> propertyValues = new LinkedHashMap<>();

	}

	private final ReportLabel reportLabel;

	public BatchStatusReport(BatchStatusReportPluginData batchStatusReportPluginData) {
		this.reportLabel = batchStatusReportPluginData.getReportLabel();
	}

	private Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	private Map<MaterialId, Set<BatchPropertyId>> batchPropertyMap = new LinkedHashMap<>();

	/*
	 * Releases a report item for each updated batch that still exists
	 */
	private void reportBatch(ReportContext reportContext, BatchRecord batchRecord) {

		// report the batch - make sure batch exists

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(batchRecord.time);
		reportItemBuilder.addValue(batchRecord.batchId);
		reportItemBuilder.addValue(batchRecord.materialsProducerId);

		if (batchRecord.stageId != null) {
			reportItemBuilder.addValue(batchRecord.stageId);
		} else {
			reportItemBuilder.addValue("");
		}

		reportItemBuilder.addValue(batchRecord.materialId);
		reportItemBuilder.addValue(batchRecord.amount);

		for (MaterialId materialId : batchPropertyMap.keySet()) {
			boolean matchingMaterial = batchRecord.materialId.equals(materialId);
			Set<BatchPropertyId> batchPropertyIds = batchPropertyMap.get(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				if (matchingMaterial) {
					reportItemBuilder.addValue(batchRecord.propertyValues.get(batchPropertyId));
				} else {
					reportItemBuilder.addValue("");
				}
			}
		}
		reportContext.releaseOutput(reportItemBuilder.build());

	}

	private ReportHeader reportHeader;

	/*
	 * Returns the ReportHeader based on the batch properties selected by the
	 * client.
	 */
	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder builder = ReportHeader	.builder()//
														.add("time")//
														.add("batch")//
														.add("materials_producer")//
														.add("stage")//
														.add("material")//
														.add("amount");//
			Set<MaterialId> materialIds = materialsDataManager.getMaterialIds();
			for (MaterialId materialId : materialIds) {
				Set<BatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(materialId);
				for (BatchPropertyId batchPropertyId : batchPropertyIds) {
					builder.add(materialId + "." + batchPropertyId);
				}
			}
			reportHeader = builder.build();
		}
		return reportHeader;

	}

	private BatchRecord createBatchRecord(ReportContext reportContext, BatchId batchId) {

		BatchRecord batchRecord = new BatchRecord();

		batchRecord.time = reportContext.getTime();
		batchRecord.batchId = batchId;
		batchRecord.materialsProducerId = materialsDataManager.getBatchProducer(batchId);
		Optional<StageId> optionalStageId = materialsDataManager.getBatchStageId(batchId);
		if (optionalStageId.isPresent()) {
			batchRecord.stageId = optionalStageId.get();
		} else {
			batchRecord.stageId = null;
		}
		batchRecord.materialId = materialsDataManager.getBatchMaterial(batchId);
		batchRecord.amount = materialsDataManager.getBatchAmount(batchId);

		Set<BatchPropertyId> batchPropertyIds = materialsDataManager.getBatchPropertyIds(batchRecord.materialId);
		for (BatchPropertyId batchPropertyId : batchPropertyIds) {
			Object batchPropertyValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
			batchRecord.propertyValues.put(batchPropertyId, batchPropertyValue);
		}
		batchRecords.put(batchId, batchRecord);
		return batchRecord;
	}

	private void handleBatchAdditionEvent(ReportContext reportContext, BatchAdditionEvent batchAdditionEvent) {
		BatchId batchId = batchAdditionEvent.batchId();
		BatchRecord batchRecord = createBatchRecord(reportContext, batchId);
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchImminentRemovalEvent(ReportContext reportContext, BatchImminentRemovalEvent batchImminentRemovalEvent) {
		BatchId batchId = batchImminentRemovalEvent.batchId();
		BatchRecord batchRecord = batchRecords.remove(batchId);
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchAmountUpdateEvent(ReportContext reportContext, BatchAmountUpdateEvent batchAmountUpdateEvent) {
		BatchId batchId = batchAmountUpdateEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.amount = batchAmountUpdateEvent.currentAmount();
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleStageMembershipAdditionEvent(ReportContext reportContext, StageMembershipAdditionEvent stageMembershipAdditionEvent) {
		BatchId batchId = stageMembershipAdditionEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = stageMembershipAdditionEvent.stageId();
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleStageMembershipRemovalEvent(ReportContext reportContext, StageMembershipRemovalEvent stageMembershipRemovalEvent) {
		BatchId batchId = stageMembershipRemovalEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = null;
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchPropertyUpdateEvent(ReportContext reportContext, BatchPropertyUpdateEvent batchPropertyUpdateEvent) {
		BatchId batchId = batchPropertyUpdateEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.propertyValues.put(batchPropertyUpdateEvent.batchPropertyId(), batchPropertyUpdateEvent.currentPropertyValue());
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private MaterialsDataManager materialsDataManager;

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(BatchAdditionEvent.class, this::handleBatchAdditionEvent);
		reportContext.subscribe(BatchImminentRemovalEvent.class, this::handleBatchImminentRemovalEvent);
		reportContext.subscribe(BatchAmountUpdateEvent.class, this::handleBatchAmountUpdateEvent);
		reportContext.subscribe(BatchPropertyUpdateEvent.class, this::handleBatchPropertyUpdateEvent);
		reportContext.subscribe(StageMembershipAdditionEvent.class, this::handleStageMembershipAdditionEvent);
		reportContext.subscribe(StageMembershipRemovalEvent.class, this::handleStageMembershipRemovalEvent);
		reportContext.subscribeToSimulationState(this::recordSimulationState);

		materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);

		for (MaterialId materialId : materialsDataManager.getMaterialIds()) {
			this.batchPropertyMap.put(materialId, materialsDataManager.getBatchPropertyIds(materialId));
		}

		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (BatchId inventoryBatchId : materialsDataManager.getInventoryBatches(materialsProducerId)) {
				BatchRecord batchRecord = createBatchRecord(reportContext, inventoryBatchId);
				reportBatch(reportContext, batchRecord);
			}
			for (StageId stageId : materialsDataManager.getStages(materialsProducerId)) {
				for (BatchId stageBatchId : materialsDataManager.getStageBatches(stageId)) {
					BatchRecord batchRecord = createBatchRecord(reportContext, stageBatchId);
					reportBatch(reportContext, batchRecord);
				}
			}
		}
	}
	
	private void recordSimulationState(ReportContext reportContext, SimulationStateContext simulationStateContext) {
		BatchStatusReportPluginData.Builder builder = simulationStateContext.get(BatchStatusReportPluginData.Builder.class);
		builder.setReportLabel(reportLabel);
	}


}
