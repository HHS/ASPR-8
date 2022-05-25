package plugins.materials.actors;

import nucleus.ActorContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.ResourceIdAdditionEvent;
import plugins.resources.support.ResourceId;

/**
 * A Report that displays materials producer resource changes over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the materials producer resource level was set
 * 
 * Resource -- the resource identifier
 *
 * MaterialsProducer -- the materials producer identifier
 *
 * Action -- the action taken on the resource
 *
 * Amount -- the amount of resource
 *
 * @author Shawn Hatch
 *
 */
public final class MaterialsProducerResourceReport {
	private final ReportId reportId;

	public MaterialsProducerResourceReport(ReportId reportId) {
		this.reportId = reportId;
	}

	private static enum Action {
		/*
		 * Used when a resource is directly added to a materials producer which
		 * only happens when the materials producer is being initialized from
		 * the scenario
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
			reportHeader = ReportHeader	.builder()//
										.add("time")//
										.add("resource")//
										.add("materials_producer")//
										.add("action")//
										.add("amount")//
										.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerResourceUpdateEvent(ActorContext actorContext, MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		long currentResourceLevel = materialsProducerResourceUpdateEvent.getCurrentResourceLevel();
		long previousResourceLevel = materialsProducerResourceUpdateEvent.getPreviousResourceLevel();
		long amount = currentResourceLevel - previousResourceLevel;
		ResourceId resourceId = materialsProducerResourceUpdateEvent.getResourceId();
		MaterialsProducerId materialsProducerId = materialsProducerResourceUpdateEvent.getMaterialsProducerId();
		if (amount > 0) {
			writeReportItem(actorContext, resourceId, materialsProducerId, Action.ADDED, amount);
		} else {
			amount = -amount;
			writeReportItem(actorContext, resourceId, materialsProducerId, Action.REMOVED, amount);
		}
	}

	private void writeReportItem(ActorContext actorContext, final ResourceId resourceId, final MaterialsProducerId materialsProducerId, final Action action, final long amount) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);
		reportItemBuilder.addValue(actorContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(amount);
		actorContext.releaseOutput(reportItemBuilder.build());
	}

	private void handleResourceIdAdditionEvent(ActorContext actorContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.getResourceId();
		MaterialsDataManager materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(actorContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
		}
	}

	public void init(final ActorContext actorContext) {

		actorContext.subscribe(MaterialsProducerResourceUpdateEvent.class, this::handleMaterialsProducerResourceUpdateEvent);
		actorContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		actorContext.subscribe(MaterialsProducerAdditionEvent.class, this::handleMaterialsProducerAdditionEvent);

		ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		MaterialsDataManager materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		for (MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				writeReportItem(actorContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
			}
		}
	}

	public void handleMaterialsProducerAdditionEvent(ActorContext actorContext, MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		ResourcesDataManager resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		MaterialsDataManager materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(actorContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
		}
	}

}
