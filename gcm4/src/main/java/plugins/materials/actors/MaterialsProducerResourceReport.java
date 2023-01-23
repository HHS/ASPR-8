package plugins.materials.actors;

import nucleus.ReportContext;
import plugins.materials.dataviews.MaterialsDataView;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.resources.dataviews.ResourcesDataView;
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

	private void handleMaterialsProducerResourceUpdateEvent(ReportContext reportContext, MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
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

	private void writeReportItem(ReportContext reportContext, final ResourceId resourceId, final MaterialsProducerId materialsProducerId, final Action action, final long amount) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(amount);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	private void handleResourceIdAdditionEvent(ReportContext reportContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class);
		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			long materialsProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
		}
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerResourceUpdateEvent.class, this::handleMaterialsProducerResourceUpdateEvent);
		reportContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		reportContext.subscribe(MaterialsProducerAdditionEvent.class, this::handleMaterialsProducerAdditionEvent);

		ResourcesDataView resourcesDataView = reportContext.getDataView(ResourcesDataView.class);
		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class);
		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (ResourceId resourceId : resourcesDataView.getResourceIds()) {
				long materialsProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
			}
		}
	}

	private void handleMaterialsProducerAdditionEvent(ReportContext reportContext, MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		ResourcesDataView resourcesDataView = reportContext.getDataView(ResourcesDataView.class);
		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class);
		for (ResourceId resourceId : resourcesDataView.getResourceIds()) {
			long materialsProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
			writeReportItem(reportContext, resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
		}
	}

}
