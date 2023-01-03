package plugins.materials.actors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventFilter;
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
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays the state of batches over time. The batch properties included in this report are limited to those present during initialization.
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

	private void handleBatchAdditionEvent(ActorContext actorContext, BatchAdditionEvent batchAdditionEvent) {
		BatchId batchId = batchAdditionEvent.batchId();
		BatchRecord batchRecord = createBatchRecord(actorContext, batchId);
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchImminentRemovalEvent(ActorContext actorContext, BatchImminentRemovalEvent batchImminentRemovalEvent) {
		BatchId batchId = batchImminentRemovalEvent.batchId();
		BatchRecord batchRecord = batchRecords.remove(batchId);
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchAmountUpdateEvent(ActorContext actorContext, BatchAmountUpdateEvent batchAmountUpdateEvent) {
		BatchId batchId = batchAmountUpdateEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.amount = batchAmountUpdateEvent.currentAmount();
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleStageMembershipAdditionEvent(ActorContext actorContext, StageMembershipAdditionEvent stageMembershipAdditionEvent) {
		BatchId batchId = stageMembershipAdditionEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = stageMembershipAdditionEvent.stageId();
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleStageMembershipRemovalEvent(ActorContext actorContext, StageMembershipRemovalEvent stageMembershipRemovalEvent) {
		BatchId batchId = stageMembershipRemovalEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = null;
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private void handleBatchPropertyUpdateEvent(ActorContext actorContext, BatchPropertyUpdateEvent batchPropertyUpdateEvent) {
		BatchId batchId = batchPropertyUpdateEvent.batchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.propertyValues.put(batchPropertyUpdateEvent.batchPropertyId(), batchPropertyUpdateEvent.currentPropertyValue());
		batchRecord.time = actorContext.getTime();
		reportBatch(actorContext, batchRecord);
	}

	private MaterialsDataManager materialsDataManager;

	public void init(final ActorContext actorContext) {

		actorContext.subscribe(EventFilter.builder(BatchAdditionEvent.class).build(), this::handleBatchAdditionEvent);
		actorContext.subscribe(EventFilter.builder(BatchImminentRemovalEvent.class).build(), this::handleBatchImminentRemovalEvent);
		actorContext.subscribe(EventFilter.builder(BatchAmountUpdateEvent.class).build(), this::handleBatchAmountUpdateEvent);
		actorContext.subscribe(EventFilter.builder(BatchPropertyUpdateEvent.class).build(), this::handleBatchPropertyUpdateEvent);
		actorContext.subscribe(EventFilter.builder(StageMembershipAdditionEvent.class).build(), this::handleStageMembershipAdditionEvent);
		actorContext.subscribe(EventFilter.builder(StageMembershipRemovalEvent.class).build(), this::handleStageMembershipRemovalEvent);

		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		
		for(MaterialId materialId : materialsDataManager.getMaterialIds()) {
			this.batchPropertyMap.put(materialId, materialsDataManager.getBatchPropertyIds(materialId));
		}

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
