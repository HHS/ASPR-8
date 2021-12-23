package plugins.materials.reports;

import nucleus.ReportContext;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.observation.MaterialsProducerResourceChangeObservationEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.resources.datacontainers.ResourceDataView;
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
										.add("Time")//
										.add("Resource")//
										.add("MaterialsProducer")//
										.add("Action")//
										.add("Amount")//
										.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerResourceChangeObservationEvent(ReportContext reportContext,MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent) {
		long currentResourceLevel = materialsProducerResourceChangeObservationEvent.getCurrentResourceLevel();
		long previousResourceLevel = materialsProducerResourceChangeObservationEvent.getPreviousResourceLevel();
		long amount = currentResourceLevel - previousResourceLevel;
		ResourceId resourceId = materialsProducerResourceChangeObservationEvent.getResourceId();
		MaterialsProducerId materialsProducerId = materialsProducerResourceChangeObservationEvent.getMaterialsProducerId();
		if (amount > 0) {
			writeReportItem(reportContext,resourceId, materialsProducerId, Action.ADDED, amount);
		} else {
			amount = -amount;
			writeReportItem(reportContext,resourceId, materialsProducerId, Action.REMOVED, amount);
		}
	}

	private void writeReportItem(ReportContext reportContext,final ResourceId resourceId, final MaterialsProducerId materialsProducerId, final Action action, final long amount) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(action.displayName);
		reportItemBuilder.addValue(amount);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerResourceChangeObservationEvent.class,this::handleMaterialsProducerResourceChangeObservationEvent);

		ResourceDataView resourceDataView = reportContext.getDataView(ResourceDataView.class).get();
		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class).get();
		for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (ResourceId resourceId : resourceDataView.getResourceIds()) {
				long materialsProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				writeReportItem(reportContext,resourceId, materialsProducerId, Action.ADDED, materialsProducerResourceLevel);
			}
		}
	}

}
