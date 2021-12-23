package plugins.compartments.reports;

import nucleus.ReportContext;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays assigned compartment property values over time via
 * {@link ReportItem} items released as output.
 *
 *
 * Fields
 *
 * Time -- the time in days when the compartment property was set
 *
 * Compartment -- the compartment identifier
 *
 * Property -- the compartment property identifier
 *
 * Value -- the value of the compartment property
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentPropertyReport {

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("Compartment")//
										.add("Property")//
										.add("Value")//
										.build();//
		}
		return reportHeader;

	}

	private void handleCompartmentPropertyChangeObservationEvent(ReportContext reportContext, CompartmentPropertyChangeObservationEvent compartmentPropertyChangeObservationEvent) {
		CompartmentId compartmentId = compartmentPropertyChangeObservationEvent.getCompartmentId();
		CompartmentPropertyId compartmentPropertyId = compartmentPropertyChangeObservationEvent.getCompartmentPropertyId();
		Object propertyValue = compartmentPropertyChangeObservationEvent.getCurrentPropertyValue();
		writeProperty(reportContext, compartmentId, compartmentPropertyId,propertyValue);
	}

	/**
	 * Initial behavior for this report. The report subscribes to
	 * {@linkplain CompartmentPropertyChangeObservationEvent} and releases a
	 * {@link ReportItem} for each compartment property's initial value.
	 */
	public void init(ReportContext reportContext) {

		reportContext.subscribe(CompartmentPropertyChangeObservationEvent.class, this::handleCompartmentPropertyChangeObservationEvent);

		CompartmentDataView compartmentDataView = reportContext.getDataView(CompartmentDataView.class).get();

		for (final CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (final CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				Object compartmentPropertyValue = compartmentDataView.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				writeProperty(reportContext, compartmentId, compartmentPropertyId,compartmentPropertyValue);
			}
		}

	}

	private void writeProperty(ReportContext reportContext, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId,final Object compartmentPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(compartmentId.toString());
		reportItemBuilder.addValue(compartmentPropertyId.toString());
		reportItemBuilder.addValue(compartmentPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}