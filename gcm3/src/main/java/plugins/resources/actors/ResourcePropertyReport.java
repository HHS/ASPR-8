package plugins.resources.actors;

import nucleus.ActorContext;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.events.ResourcePropertyUpdateEvent;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * A Report that displays assigned resource property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global resource was set
 *
 * Resource -- the resource identifier
 *
 * Property -- the resource property identifier
 *
 * Value -- the value of the resource property
 *
 * @author Shawn Hatch
 *
 */
public final class ResourcePropertyReport {
	private final ReportId reportId;
	public ResourcePropertyReport(ReportId reportId) {
		this.reportId = reportId;
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("time")//
										.add("resource")//
										.add("property")//
										.add("value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleResourcePropertyUpdateEvent(ActorContext actorContext,ResourcePropertyUpdateEvent resourcePropertyUpdateEvent) {
		ResourceId resourceId = resourcePropertyUpdateEvent.getResourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyUpdateEvent.getResourcePropertyId();
		Object currentPropertyValue = resourcePropertyUpdateEvent.getCurrentPropertyValue();
		writeProperty(actorContext,resourceId, resourcePropertyId,currentPropertyValue);
	}

	private ResourceDataManager resourceDataManager;

	public void init(final ActorContext actorContext) {

		actorContext.subscribe(ResourcePropertyUpdateEvent.class,this::handleResourcePropertyUpdateEvent);

		
		resourceDataManager = actorContext.getDataManager(ResourceDataManager.class).get();
		for (final ResourceId resourceId : resourceDataManager.getResourceIds()) {
			for (final ResourcePropertyId resourcePropertyId : resourceDataManager.getResourcePropertyIds(resourceId)) {
				Object resourcePropertyValue = resourceDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
				writeProperty(actorContext,resourceId, resourcePropertyId,resourcePropertyValue);
			}
		}
	}

	private void writeProperty(ActorContext actorContext,final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,Object resourcePropertyValue) {

		
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);

		reportItemBuilder.addValue(actorContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(resourcePropertyId.toString());
		reportItemBuilder.addValue(resourcePropertyValue);
		actorContext.releaseOutput(reportItemBuilder.build());
	}

}