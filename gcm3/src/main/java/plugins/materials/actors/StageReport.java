package plugins.materials.actors;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ActorContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.StageAdditionEvent;
import plugins.materials.events.StageImminentRemovalEvent;
import plugins.materials.events.StageMaterialsProducerUpdateEvent;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
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
	private final ReportId reportId;

	public StageReport(ReportId reportId) {
		this.reportId = reportId;
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
			reportHeader = ReportHeader	.builder()//
										.add("time")//
										.add("stage")//
										.add("materials_producer")//
										.add("action")//
										.add("offered")//
										.build();//
		}
		return reportHeader;
	}

	private void handleStageAdditionEvent(ActorContext actorContext, StageAdditionEvent stageAdditionEvent) {

		StageRecord stageRecord = new StageRecord();
		stageRecord.stageId = stageAdditionEvent.getStageId();
		stageRecord.isOffered = materialsDataManager.isStageOffered(stageRecord.stageId);
		stageRecord.materialsProducerId = materialsDataManager.getStageProducer(stageRecord.stageId);
		stageRecord.lastAction = Action.CREATED;
		stageRecords.put(stageRecord.stageId, stageRecord);
		writeReportItem(actorContext, stageRecord);
	}

	private void handleStageImminentRemovalEvent(ActorContext actorContext, StageImminentRemovalEvent stageImminentRemovalEvent) {
		StageId stageId = stageImminentRemovalEvent.getStageId();
		StageRecord stageRecord = stageRecords.remove(stageId);
		stageRecord.lastAction = Action.DESTROYED;
		writeReportItem(actorContext, stageRecord);
	}

	private void handleStageOfferUpdateEvent(ActorContext actorContext, StageOfferUpdateEvent stageOfferUpdateEvent) {
		StageId stageId = stageOfferUpdateEvent.getStageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.isOffered = stageOfferUpdateEvent.isCurrentOfferState();
		stageRecord.lastAction = Action.OFFERED;
		writeReportItem(actorContext, stageRecord);
	}

	private void handleStageMaterialsProducerUpdateEvent(ActorContext actorContext, StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent) {
		StageId stageId = stageMaterialsProducerUpdateEvent.getStageId();
		StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.materialsProducerId = stageMaterialsProducerUpdateEvent.getCurrentMaterialsProducerId();
		stageRecord.lastAction = Action.TRANSFERRED;
		writeReportItem(actorContext, stageRecord);
	}

	private void writeReportItem(ActorContext actorContext, final StageRecord stageRecord) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);
		reportItemBuilder.addValue(actorContext.getTime());
		reportItemBuilder.addValue(stageRecord.stageId);
		reportItemBuilder.addValue(stageRecord.materialsProducerId.toString());
		reportItemBuilder.addValue(stageRecord.lastAction.displayName);
		reportItemBuilder.addValue(stageRecord.isOffered);
		actorContext.releaseOutput(reportItemBuilder.build());
	}

	private MaterialsDataManager materialsDataManager;

	public void init(final ActorContext actorContext) {

		actorContext.subscribe(StageOfferUpdateEvent.class, this::handleStageOfferUpdateEvent);
		actorContext.subscribe(StageAdditionEvent.class, this::handleStageAdditionEvent);
		actorContext.subscribe(StageImminentRemovalEvent.class, this::handleStageImminentRemovalEvent);
		actorContext.subscribe(StageMaterialsProducerUpdateEvent.class, this::handleStageMaterialsProducerUpdateEvent);

		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);

		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (StageId stageId : materialsDataManager.getStages(materialsProducerId)) {

				StageRecord stageRecord = new StageRecord();
				stageRecord.stageId = stageId;
				stageRecord.isOffered = materialsDataManager.isStageOffered(stageId);
				stageRecord.materialsProducerId = materialsProducerId;
				stageRecord.lastAction = Action.CREATED;
				stageRecords.put(stageRecord.stageId, stageRecord);

				writeReportItem(actorContext, stageRecord);
			}
		}
	}

}