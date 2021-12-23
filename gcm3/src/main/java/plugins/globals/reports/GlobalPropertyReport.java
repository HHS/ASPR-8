package plugins.globals.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.EventLabel;
import nucleus.ReportContext;
import plugins.globals.datacontainers.GlobalDataView;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;
import plugins.globals.support.GlobalPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays assigned global property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the global property was set
 *
 * Property -- the global property identifier
 *
 * Value -- the value of the global property
 *
 * @author Shawn Hatch
 *
 */
public final class GlobalPropertyReport {

	private ReportHeader reportHeader;

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<GlobalPropertyId> globalPropertyIds = new LinkedHashSet<>();

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("Property")//
										.add("Value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleGlobalPropertyChangeObservationEvent(ReportContext reportContext, GlobalPropertyChangeObservationEvent globalPropertyChangeObservationEvent) {
		GlobalPropertyId globalPropertyId = globalPropertyChangeObservationEvent.getGlobalPropertyId();
		if (globalPropertyIds.contains(globalPropertyId)) {
			writeProperty(reportContext, globalPropertyId,globalPropertyChangeObservationEvent.getCurrentPropertyValue());
		}
	}

	

	public GlobalPropertyReport(GlobalPropertyId... globalPropertyIds) {
		for (GlobalPropertyId globalPropertyId : globalPropertyIds) {
			this.globalPropertyIds.add(globalPropertyId);
		}
	}

	public void init(final ReportContext reportContext) {

		GlobalDataView globalDataView = reportContext.getDataView(GlobalDataView.class).get();

		/*
		 * If no global properties were specified, then assume all are wanted
		 */
		if (globalPropertyIds.size() == 0) {
			globalPropertyIds.addAll(globalDataView.getGlobalPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<GlobalPropertyId> validPropertyIds = globalDataView.getGlobalPropertyIds();
		for (final GlobalPropertyId globalPropertyId : globalPropertyIds) {
			if (!validPropertyIds.contains(globalPropertyId)) {
				throw new RuntimeException("invalid property id " + globalPropertyId);
			}
		}

		if (globalPropertyIds.equals(globalDataView.getGlobalPropertyIds())) {
			reportContext.subscribe(GlobalPropertyChangeObservationEvent.class, this::handleGlobalPropertyChangeObservationEvent);
		} else {
			for (GlobalPropertyId globalPropertyId : globalPropertyIds) {
				EventLabel<GlobalPropertyChangeObservationEvent> eventLabel = GlobalPropertyChangeObservationEvent.getEventLabel(reportContext, globalPropertyId);
				reportContext.subscribe(eventLabel, this::handleGlobalPropertyChangeObservationEvent);
			}
		}

		for (final GlobalPropertyId globalPropertyId : globalPropertyIds) {
			Object globalPropertyValue = globalDataView.getGlobalPropertyValue(globalPropertyId);
			writeProperty(reportContext, globalPropertyId,globalPropertyValue);
		}
	}

	private void writeProperty(ReportContext reportContext, final GlobalPropertyId globalPropertyId,Object globalPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());

		
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(globalPropertyId.toString());
		reportItemBuilder.addValue(globalPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}