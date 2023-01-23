package plugins.resources.actors;

import nucleus.ReportContext;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.resources.dataviews.ResourcesDataView;
import plugins.resources.events.ResourcePropertyDefinitionEvent;
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

	private void handleResourcePropertyUpdateEvent(ReportContext reportContext,ResourcePropertyUpdateEvent resourcePropertyUpdateEvent) {
		ResourceId resourceId = resourcePropertyUpdateEvent.resourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyUpdateEvent.resourcePropertyId();
		Object currentPropertyValue = resourcePropertyUpdateEvent.currentPropertyValue();
		writeProperty(reportContext,resourceId, resourcePropertyId,currentPropertyValue);
	}


	public void init(final ReportContext reportContext) {

		reportContext.subscribe(ResourcePropertyUpdateEvent.class,this::handleResourcePropertyUpdateEvent);
		reportContext.subscribe(ResourcePropertyDefinitionEvent.class, this::handleResourcePropertyAdditionEvent);
		
		ResourcesDataView resourcesDataView = reportContext.getDataView(ResourcesDataView.class);
		for (final ResourceId resourceId : resourcesDataView.getResourceIds()) {
			for (final ResourcePropertyId resourcePropertyId : resourcesDataView.getResourcePropertyIds(resourceId)) {
				Object resourcePropertyValue = resourcesDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
				writeProperty(reportContext,resourceId, resourcePropertyId,resourcePropertyValue);
			}
		}				
	}
	
	private void handleResourcePropertyAdditionEvent(ReportContext reportContext, ResourcePropertyDefinitionEvent resourcePropertyDefinitionEvent) {
		ResourceId resourceId = resourcePropertyDefinitionEvent.resourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyDefinitionEvent.resourcePropertyId();		
		Object resourcePropertyValue = resourcePropertyDefinitionEvent.resourcePropertyValue();
		writeProperty(reportContext,resourceId, resourcePropertyId,resourcePropertyValue);
	}

	private void writeProperty(ReportContext reportContext,final ResourceId resourceId, final ResourcePropertyId resourcePropertyId,Object resourcePropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(resourceId.toString());
		reportItemBuilder.addValue(resourcePropertyId.toString());
		reportItemBuilder.addValue(resourcePropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}