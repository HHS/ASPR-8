package plugins.materials.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ReportContext;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.observation.StageCreationObservationEvent;
import plugins.materials.events.observation.StageImminentRemovalObservationEvent;
import plugins.materials.events.observation.StageMaterialsProducerChangeObservationEvent;
import plugins.materials.events.observation.StageOfferChangeObservationEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays the creation, destruction, offering, batch conversion,
 * resource conversion and transfer of stages.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global resource was set
 *
 * Stage -- the stage identifier
 *
 * MaterialsProducer -- the acting materials producer
 *
 * Action -- One of Create, Destroy, Offer, BatchConversion, ResourceConversion,
 * Transfer
 * 
 * Offered -- the offered state of the stage
 * 
 * ResourceMaterial
 * 
 * Amount
 *
 * @author Shawn Hatch
 *
 */
public final class StageReport {

	private static class StageRecord {
		StageId stageId;
		MaterialsProducerId materialsProducerId;
		boolean isOffered;
		Action lastAction;
	}

	private Map<StageId, StageRecord> stageRecords = new LinkedHashMap<>();

	/*
	 * An enumeration mirroring the cause of a change to a stage
	 */
	private static enum Action {
		CREATED("Create"),

		DESTROYED("Destroy"),

		OFFERED("Offer"),

		TRANSFERRED("Transfer");

		private final String displayName;

		private Action(final String displayName) {
			this.displayName = displayName;
		}
	}

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("Stage")//
										.add("MaterialsProducer")//
										.add("Action")//
										.add("Offered")//
										.build();//
		}
		return reportHeader;
	}

	private void handleStageCreationObservationEvent(ReportContext reportContext, StageCreationObservationEvent stageCreationObservationEvent) {

		StageRecord stageRecord = new StageRecord();
		stageRecord.stageId = stageCreationObservationEvent.getStageId();
		stageRecord.isOffered = materialsDataView.isStageOffered(stageRecord.stageId);
		stageRecord.materialsProducerId = materialsDataView.getStageProducer(stageRecord.stageId);
		stageRecord.lastAction = Action.CREATED;
		stageRecords.put(stageRecord.stageId, stageRecord);
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageDestructionObservationEvent(ReportContext reportContext, StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent) {
		StageId stageId = stageImminentRemovalObservationEvent.getStageId();
		StageRecord stageRecord = stageRecords.remove(stageId);
		stageRecord.lastAction = Action.DESTROYED;
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageOfferChangeObservationEvent(ReportContext reportContext, StageOfferChangeObservationEvent stageOfferChangeObservationEvent) {
		StageId stageId = stageOfferChangeObservationEvent.getStageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.isOffered = stageOfferChangeObservationEvent.isCurrentOfferState();
		stageRecord.lastAction = Action.OFFERED;
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageMaterialsProducerChangeObservationEvent(ReportContext reportContext, StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent) {
		StageId stageId = stageMaterialsProducerChangeObservationEvent.getStageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.materialsProducerId = stageMaterialsProducerChangeObservationEvent.getCurrentMaterialsProducerId();
		stageRecord.lastAction = Action.TRANSFERRED;
		writeReportItem(reportContext, stageRecord);
	}

	private void writeReportItem(ReportContext reportContext, final StageRecord stageRecord) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(stageRecord.stageId);
		reportItemBuilder.addValue(stageRecord.materialsProducerId.toString());
		reportItemBuilder.addValue(stageRecord.lastAction.displayName);
		reportItemBuilder.addValue(stageRecord.isOffered);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	private MaterialsDataView materialsDataView;

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(StageOfferChangeObservationEvent.class, this::handleStageOfferChangeObservationEvent);
		reportContext.subscribe(StageCreationObservationEvent.class, this::handleStageCreationObservationEvent);
		reportContext.subscribe(StageImminentRemovalObservationEvent.class, this::handleStageDestructionObservationEvent);
		reportContext.subscribe(StageMaterialsProducerChangeObservationEvent.class, this::handleStageMaterialsProducerChangeObservationEvent);

		materialsDataView = reportContext.getDataView(MaterialsDataView.class).get();

		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (StageId stageId : materialsDataView.getStages(materialsProducerId)) {

				StageRecord stageRecord = new StageRecord();
				stageRecord.stageId = stageId;
				stageRecord.isOffered = materialsDataView.isStageOffered(stageId);
				stageRecord.materialsProducerId = materialsProducerId;
				stageRecord.lastAction = Action.CREATED;
				stageRecords.put(stageRecord.stageId, stageRecord);

				writeReportItem(reportContext, stageRecord);
			}
		}
	}

}
