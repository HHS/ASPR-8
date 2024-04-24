package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.StageAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.StageImminentRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.StageMaterialsProducerUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.StageOfferUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;

/**
 * A Report that displays the creation, destruction, offering, batch conversion,
 * resource conversion and transfer of stages. Fields Time -- the time in days
 * when the global resource was set Stage -- the stage identifier
 * MaterialsProducer -- the acting materials producer Action -- One of Create,
 * Destroy, Offer, BatchConversion, ResourceConversion, Transfer Offered -- the
 * offered state of the stage ResourceMaterial Amount
 */
public final class StageReport {
	private final ReportLabel reportLabel;

	public StageReport(StageReportPluginData stageReportPluginData) {
		this.reportLabel = stageReportPluginData.getReportLabel();
	}

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
			reportHeader = ReportHeader.builder()//
					.add("time")//
					.add("stage")//
					.add("materials_producer")//
					.add("action")//
					.add("offered")//
					.build();//
		}
		return reportHeader;
	}

	private void handleStageAdditionEvent(ReportContext reportContext, StageAdditionEvent stageAdditionEvent) {

		StageRecord stageRecord = new StageRecord();
		stageRecord.stageId = stageAdditionEvent.stageId();
		stageRecord.isOffered = materialsDataManager.isStageOffered(stageRecord.stageId);
		stageRecord.materialsProducerId = materialsDataManager.getStageProducer(stageRecord.stageId);
		stageRecord.lastAction = Action.CREATED;
		stageRecords.put(stageRecord.stageId, stageRecord);
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageImminentRemovalEvent(ReportContext reportContext,
			StageImminentRemovalEvent stageImminentRemovalEvent) {
		StageId stageId = stageImminentRemovalEvent.stageId();
		StageRecord stageRecord = stageRecords.remove(stageId);
		stageRecord.lastAction = Action.DESTROYED;
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageOfferUpdateEvent(ReportContext reportContext, StageOfferUpdateEvent stageOfferUpdateEvent) {
		StageId stageId = stageOfferUpdateEvent.stageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.isOffered = stageOfferUpdateEvent.currentOfferState();
		stageRecord.lastAction = Action.OFFERED;
		writeReportItem(reportContext, stageRecord);
	}

	private void handleStageMaterialsProducerUpdateEvent(ReportContext reportContext,
			StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent) {
		StageId stageId = stageMaterialsProducerUpdateEvent.stageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.materialsProducerId = stageMaterialsProducerUpdateEvent.currentMaterialsProducerId();
		stageRecord.lastAction = Action.TRANSFERRED;
		writeReportItem(reportContext, stageRecord);
	}

	private void writeReportItem(ReportContext reportContext, final StageRecord stageRecord) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(stageRecord.stageId);
		reportItemBuilder.addValue(stageRecord.materialsProducerId.toString());
		reportItemBuilder.addValue(stageRecord.lastAction.displayName);
		reportItemBuilder.addValue(stageRecord.isOffered);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	private MaterialsDataManager materialsDataManager;

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(StageOfferUpdateEvent.class, this::handleStageOfferUpdateEvent);
		reportContext.subscribe(StageAdditionEvent.class, this::handleStageAdditionEvent);
		reportContext.subscribe(StageImminentRemovalEvent.class, this::handleStageImminentRemovalEvent);
		reportContext.subscribe(StageMaterialsProducerUpdateEvent.class, this::handleStageMaterialsProducerUpdateEvent);
		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);

		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (StageId stageId : materialsDataManager.getStages(materialsProducerId)) {

				StageRecord stageRecord = new StageRecord();
				stageRecord.stageId = stageId;
				stageRecord.isOffered = materialsDataManager.isStageOffered(stageId);
				stageRecord.materialsProducerId = materialsProducerId;
				stageRecord.lastAction = Action.CREATED;
				stageRecords.put(stageRecord.stageId, stageRecord);

				writeReportItem(reportContext, stageRecord);
			}
		}
	}

	private void recordSimulationState(ReportContext reportContext) {
		StageReportPluginData.Builder builder = StageReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		reportContext.releaseOutput(builder.build());
	}

}
