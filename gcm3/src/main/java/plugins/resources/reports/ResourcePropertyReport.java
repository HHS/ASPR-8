package plugins.resources.reports;

import nucleus.ReportContext;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.observation.ResourcePropertyChangeObservationEvent;
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

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("Resource")//
										.add("Property")//
										.add("Value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleResourcePropertyChangeObservationEvent(ReportContext reportContext,ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent) {
		ResourceId resourceId = resourcePropertyChangeObservationEvent.getResourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyChangeObservationEvent.getResourcePropertyId();
		Object currentPropertyValue = resourcePropertyChangeObservationEvent.getCurrentPropertyValue();
		writeProperty(reportContext,resourceId, resourcePropertyId,currentPropertyValue);
	}

	private ResourceDataView resourceDataView;

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(ResourcePropertyChangeObservationEvent.class,this::handleResourcePropertyChangeObservationEvent);

		
		resourceDataView = reportContext.getDataView(ResourceDataView.class).get();
		for (final ResourceId resourceId : resourceDataView.getResourceIds()) {
			for (final ResourcePropertyId resourcePropertyId : resourceDataView.getResourcePropertyIds(resourceId)) {
				Object resourcePropertyValue = resourceDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
				writeProperty(reportContext,resourceId, resourcePropertyId,resourcePropertyValue);
			}
		}
	}

	private void writeProperty(ReportContext reportContext,final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,Object resourcePropertyValue) {

		
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(resourcePropertyId.toString());
		reportItemBuilder.addValue(resourcePropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}