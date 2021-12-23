package plugins.materials.reports;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.ReportContext;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.observation.BatchAmountChangeObservationEvent;
import plugins.materials.events.observation.BatchCreationObservationEvent;
import plugins.materials.events.observation.BatchImminentRemovalObservationEvent;
import plugins.materials.events.observation.BatchPropertyChangeObservationEvent;
import plugins.materials.events.observation.StageMembershipAdditionObservationEvent;
import plugins.materials.events.observation.StageMembershipRemovalObservationEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.reports.support.ReportHeader;
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

	private Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	private Map<MaterialId, Set<BatchPropertyId>> batchPropertyMap = new LinkedHashMap<>();

	/*
	 * Releases a report item for each updated batch that still exists
	 */
	private void reportBatch(ReportContext reportContext, BatchRecord batchRecord) {

		// report the batch - make sure batch exists

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());
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
														.add("Time")//
														.add("Batch")//
														.add("MaterialsProducer")//
														.add("Stage").add("Material")//
														.add("Amount");//
			Set<MaterialId> materialIds = materialsDataView.getMaterialIds();
			for (MaterialId materialId : materialIds) {
				Set<BatchPropertyId> batchPropertyIds = materialsDataView.getBatchPropertyIds(materialId);
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
		batchRecord.materialsProducerId = materialsDataView.getBatchProducer(batchId);
		Optional<StageId> optionalStageId = materialsDataView.getBatchStageId(batchId);
		if (optionalStageId.isPresent()) {
			batchRecord.stageId = optionalStageId.get();
		} else {
			batchRecord.stageId = null;
		}
		batchRecord.materialId = materialsDataView.getBatchMaterial(batchId);
		batchRecord.amount = materialsDataView.getBatchAmount(batchId);

		Set<BatchPropertyId> batchPropertyIds = materialsDataView.getBatchPropertyIds(batchRecord.materialId);
		for (BatchPropertyId batchPropertyId : batchPropertyIds) {
			Object batchPropertyValue = materialsDataView.getBatchPropertyValue(batchId, batchPropertyId);
			batchRecord.propertyValues.put(batchPropertyId, batchPropertyValue);
		}
		batchRecords.put(batchId, batchRecord);
		return batchRecord;
	}

	private void handleBatchCreationObservationEvent(ReportContext reportContext, BatchCreationObservationEvent batchCreationObservationEvent) {
		BatchId batchId = batchCreationObservationEvent.getBatchId();
		BatchRecord batchRecord = createBatchRecord(reportContext, batchId);
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchDestructionObservationEvent(ReportContext reportContext, BatchImminentRemovalObservationEvent batchImminentRemovalObservationEvent) {
		BatchId batchId = batchImminentRemovalObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.remove(batchId);
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchAmountChangeObservationEvent(ReportContext reportContext, BatchAmountChangeObservationEvent batchAmountChangeObservationEvent) {
		BatchId batchId = batchAmountChangeObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.amount = batchAmountChangeObservationEvent.getCurrentAmount();
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleStageMembershipAdditionObservationEvent(ReportContext reportContext, StageMembershipAdditionObservationEvent stageMembershipAdditionObservationEvent) {
		BatchId batchId = stageMembershipAdditionObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = stageMembershipAdditionObservationEvent.getStageId();
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleStageMembershipRemovalObservationEvent(ReportContext reportContext, StageMembershipRemovalObservationEvent stageMembershipRemovalObservationEvent) {
		BatchId batchId = stageMembershipRemovalObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.stageId = null;
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private void handleBatchPropertyChangeObservationEvent(ReportContext reportContext, BatchPropertyChangeObservationEvent batchPropertyChangeObservationEvent) {
		BatchId batchId = batchPropertyChangeObservationEvent.getBatchId();
		BatchRecord batchRecord = batchRecords.get(batchId);
		batchRecord.propertyValues.put(batchPropertyChangeObservationEvent.getBatchPropertyId(), batchPropertyChangeObservationEvent.getCurrentPropertyValue());
		batchRecord.time = reportContext.getTime();
		reportBatch(reportContext, batchRecord);
	}

	private MaterialsDataView materialsDataView;

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(BatchCreationObservationEvent.class, this::handleBatchCreationObservationEvent);
		reportContext.subscribe(BatchImminentRemovalObservationEvent.class, this::handleBatchDestructionObservationEvent);
		reportContext.subscribe(BatchAmountChangeObservationEvent.class, this::handleBatchAmountChangeObservationEvent);
		reportContext.subscribe(BatchPropertyChangeObservationEvent.class, this::handleBatchPropertyChangeObservationEvent);
		reportContext.subscribe(StageMembershipAdditionObservationEvent.class, this::handleStageMembershipAdditionObservationEvent);
		reportContext.subscribe(StageMembershipRemovalObservationEvent.class, this::handleStageMembershipRemovalObservationEvent);

		materialsDataView = reportContext.getDataView(MaterialsDataView.class).get();

		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (BatchId inventoryBatchId : materialsDataView.getInventoryBatches(materialsProducerId)) {
				BatchRecord batchRecord = createBatchRecord(reportContext, inventoryBatchId);
				reportBatch(reportContext, batchRecord);				
			}
			for (StageId stageId : materialsDataView.getStages(materialsProducerId)) {
				for (BatchId stageBatchId : materialsDataView.getStageBatches(stageId)) {					
					BatchRecord batchRecord = createBatchRecord(reportContext, stageBatchId);
					reportBatch(reportContext, batchRecord);				
				}
			}
		}
	}

}
