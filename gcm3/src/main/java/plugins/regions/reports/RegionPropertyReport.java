package plugins.regions.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.EventLabel;
import nucleus.ReportContext;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.events.observation.RegionPropertyChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A Report that displays assigned region property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the region property was set
 *
 * Region -- the region identifier
 *
 * Property -- the region property identifier
 *
 * Value -- the value of the region property
 *
 * @author Shawn Hatch
 *
 */
public final class RegionPropertyReport {

	private ReportHeader reportHeader;

	/*
	 * The constrained set of person properties that will be used in this
	 * report. They are set during init()
	 */
	private final Set<RegionPropertyId> regionPropertyIds = new LinkedHashSet<>();

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("Region")//
										.add("Property")//
										.add("Value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleRegionPropertyChangeObservationEvent(ReportContext reportContext, RegionPropertyChangeObservationEvent regionPropertyChangeObservationEvent) {
		RegionId regionId = regionPropertyChangeObservationEvent.getRegionId();
		RegionPropertyId regionPropertyId = regionPropertyChangeObservationEvent.getRegionPropertyId();
		Object propertyValue = regionPropertyChangeObservationEvent.getCurrentPropertyValue();
		if (regionPropertyIds.contains(regionPropertyId)) {
			writeProperty(reportContext, regionId, regionPropertyId,propertyValue);
		}
	}

	

	public RegionPropertyReport(RegionPropertyId... regionPropertyIds) {
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			this.regionPropertyIds.add(regionPropertyId);
		}
	}
	/**
	 * Initial behavior for this report. The report subscribes to
	 * {@linkplain RegionPropertyChangeObservationEvent} and releases a
	 * {@link ReportItem} for each region property's initial value.
	 */
	public void init(final ReportContext reportContext) {
		RegionDataView regionDataView = reportContext.getDataView(RegionDataView.class).get();

		/*
		 * If no region properties were specified, then assume all are wanted
		 */
		if (regionPropertyIds.size() == 0) {
			regionPropertyIds.addAll(regionDataView.getRegionPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<RegionPropertyId> validPropertyIds = regionDataView.getRegionPropertyIds();
		for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
			if (!validPropertyIds.contains(regionPropertyId)) {
				throw new RuntimeException("invalid property id " + regionPropertyId);
			}
		}

		if (regionPropertyIds.equals(regionDataView.getRegionPropertyIds())) {
			reportContext.subscribe(RegionPropertyChangeObservationEvent.class, this::handleRegionPropertyChangeObservationEvent);
		} else {
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				EventLabel<RegionPropertyChangeObservationEvent> eventLabelByProperty = RegionPropertyChangeObservationEvent.getEventLabelByProperty(reportContext, regionPropertyId);
				reportContext.subscribe(eventLabelByProperty, this::handleRegionPropertyChangeObservationEvent);
			}
		}

		for (final RegionId regionId : regionDataView.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object propertyValue = regionDataView.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId,propertyValue);
			}
		}

	}

	private void writeProperty(ReportContext reportContext, final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {

		
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}