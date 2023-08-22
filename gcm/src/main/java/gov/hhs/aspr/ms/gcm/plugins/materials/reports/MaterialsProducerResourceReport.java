package gov.hhs.aspr.ms.gcm.plugins.materials.reports;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.materials.events.MaterialsProducerAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.resources.events.ResourceIdAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;

/**
 * A Report that displays materials producer resource changes over time. Fields
 * Time -- the time in days when the materials producer resource level was set
 * Resource -- the resource identifier MaterialsProducer -- the materials
 * producer identifier Action -- the action taken on the resource Amount -- the
 * amount of resource
 */
public final class MaterialsProducerResourceReport {
	private final ReportLabel reportLabel;

	public MaterialsProducerResourceReport(
			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData) {
		this.reportLabel = materialsProducerResourceReportPluginData.getReportLabel();
	}

	private static enum Action {
		/*
		 * Used when a resource is directly added to a materials producer which only
		 * happens when the materials producer is being initialized from the scenario
		 */
		ADDED("Added"),

		/*
		 * Used when a materials producer transfers resource to a region
		 */
		REMOVED("Removed");

		private final String displayName;

		private Action(final String displayName) {
			this.displayName = displayName;
		}
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader.builder()//
					.add("time")//
					.add("resource")//
					.add("materials_producer")//
					.add("action")//
					.add("amount")//
					.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerResourceUpdateEvent(ReportContext reportContext,
			MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		long currentResourceLevel = materialsProducerResourceUpdateEvent.currentResourceLevel();
		long previousResourceLevel = materialsProducerResourceUpdateEvent.previousResourceLevel();
		long amount = currentResourceLevel - previousResourceLevel;
		ResourceId resourceId = materialsProducerResourceUpdateEvent.resourceId();
		MaterialsProducerId materialsProducerId = materialsProducerResourceUpdateEvent.materialsProducerId();
		if (amount > 0) {
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED, amount);
		} else {
			amount = -amount;
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.REMOVED, amount);
		}
	}

	private void writeReportItem(ReportContext reportContext, final ResourceId resourceId,
			final MaterialsProducerId materialsProducerId, final Action action, final long amount) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(amount);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	private void handleResourceIdAdditionEvent(ReportContext reportContext,
			ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			long materialsProducerResourceLevel = materialsDataManager
					.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED,
					materialsProducerResourceLevel);
		}
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerResourceUpdateEvent.class,
				this::handleMaterialsProducerResourceUpdateEvent);
		reportContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		reportContext.subscribe(MaterialsProducerAdditionEvent.class, this::handleMaterialsProducerAdditionEvent);
		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		ResourcesDataManager resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				long materialsProducerResourceLevel = materialsDataManager
						.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED,
						materialsProducerResourceLevel);
			}
		}
	}

	private void recordSimulationState(ReportContext reportContext) {
		MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		reportContext.releaseOutput(builder.build());
	}

	private void handleMaterialsProducerAdditionEvent(ReportContext reportContext,
			MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		ResourcesDataManager resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			long materialsProducerResourceLevel = materialsDataManager
					.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED,
					materialsProducerResourceLevel);
		}
	}

}
