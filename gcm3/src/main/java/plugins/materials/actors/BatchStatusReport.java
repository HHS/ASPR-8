package plugins.materials.actors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.ActorContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.BatchAmountChangeObservationEvent;
import plugins.materials.events.BatchCreationObservationEvent;
import plugins.materials.events.BatchImminentRemovalObservationEvent;
import plugins.materials.events.BatchPropertyChangeObservationEvent;
import plugins.materials.events.StageMembershipAdditionObservationEvent;
import plugins.materials.events.StageMembershipRemovalObservationEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays the state of batches over time.
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
 * @author Shawn Hatch
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
	private  final ReportId reportId;
	public BatchStatusReport(ReportId reportId) {
		this.reportId = reportId;
	}

	private Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	private Map<MaterialId, Set<BatchPropertyId>> batchPropertyMap = new LinkedHashMap<>();

	/*
	 * Releases a report item for each updated batch that still exists
	 */
	private void reportBatch(ActorContext actorContext, BatchRecord batchRecord) {

		// report the batch - make sure batch exists

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);
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
		actorContext.releaseOutput(reportItemBuilder.build());

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
	
	private BatchRecord createBatchRecord(ActorContext actorContext, BatchId batchId) {
		
		BatchRecord batchRecord = new BatchRecord();

		batchRecord.time = actorContext.getTime();
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

	private void handleBatchCreationObservationEvent(ActorContext actorContext, BatchCreationObservationEvent batchCreationObservationEvent) {
		BatchId batchId = batchCreationObservationEvent.getBatchId();
		BatchRecord batchRecord = createBatchRecord(actorContext, batchId);
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchDestructionObservationEvent(ActorContext actorContext, BatchImminentRemovalObservationEvent batchImminentRemovalObservationEvent) {
		BatchId batchId = batchImminentRemovalObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.remove(batchId);
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchAmountChangeObservationEvent(ActorContext actorContext, BatchAmountChangeObservationEvent batchAmountChangeObservationEvent) {
		BatchId batchId = batchAmountChangeObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.amount = batchAmountChangeObservationEvent.getCurrentAmount();
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleStageMembershipAdditionObservationEvent(ActorContext actorContext, StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent) {
		BatchId batchId = stageMembershipAdditionObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = stageMembershipAdditionObservationEvent.getStageId();
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleStageMembershipRemovalObservationEvent(ActorContext actorContext, StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent) {
		BatchId batchId = stageMembershipRemovalObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = null;
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchPropertyChangeObservationEvent(ActorContext actorContext, BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent) {
		BatchId batchId = batchPropertyChangeObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.propertyValues.put(batchPropertyChangeObservationEvent.getBatchPropertyId(), batchPropertyChangeObservationEvent.getCurrentPropertyValue());
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private MaterialsDataManager materialsDataManager;

	public void init(final ActorContext actorContext) {

		actorContext.subscribe(BatchCreationObservationEvent.class, this::handleBatchCreationObservationEvent);
		actorContext.subscribe(BatchImminentRemovalObservationEvent.class, this::handleBatchDestructionObservationEvent);
		actorContext.subscribe(BatchAmountChangeObservationEvent.class, this::handleBatchAmountChangeObservationEvent);
		actorContext.subscribe(BatchPropertyChangeObservationEvent.class, this::handleBatchPropertyChangeObservationEvent);
		actorContext.subscribe(StageMembershipAdditionObservationEvent.class, this::handleStageMembershipAdditionObservationEvent);
		actorContext.subscribe(StageMembershipRemovalObservationEvent.class, this::handleStageMembershipRemovalObservationEvent);

		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class).get();

		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (BatchId inventoryBatchId : materialsDataManager.getInventoryBatches(materialsProducerId)) {
				BatchRecord batchRecord = createBatchRecord(actorContext, inventoryBatchId);
				reportBatch(actorContext, batchRecord);				
			}
			for (StageId stageId : materialsDataManager.getStages(materialsProducerId)) {
				for (BatchId stageBatchId : materialsDataManager.getStageBatches(stageId)) {					
					BatchRecord batchRecord = createBatchRecord(actorContext, stageBatchId);
					reportBatch(actorContext, batchRecord);				
				}
			}
		}
	}

}
