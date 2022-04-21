package plugins.regions.actors;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventLabel;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
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

	private void handleRegionPropertyUpdateEvent(ActorContext actorContext, RegionPropertyUpdateEvent regionPropertyUpdateEvent) {
		RegionId regionId = regionPropertyUpdateEvent.getRegionId();
		RegionPropertyId regionPropertyId = regionPropertyUpdateEvent.getRegionPropertyId();
		Object propertyValue = regionPropertyUpdateEvent.getCurrentPropertyValue();
		if (regionPropertyIds.contains(regionPropertyId)) {
			writeProperty(actorContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private final ReportId reportId;

	public RegionPropertyReport(ReportId reportId, RegionPropertyId... regionPropertyIds) {
		this.reportId = reportId;
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			this.regionPropertyIds.add(regionPropertyId);
		}
	}

	/**
	 * Initial behavior for this report. The report subscribes to
	 * {@linkplain RegionPropertyUpdateEvent} and releases a
	 * {@link ReportItem} for each region property's initial value.
	 */
	public void init(final ActorContext actorContext) {
		RegionDataManager regionDataManager = actorContext.getDataManager(RegionDataManager.class);

		/*
		 * If no region properties were specified, then assume all are wanted
		 */
		if (regionPropertyIds.size() == 0) {
			regionPropertyIds.addAll(regionDataManager.getRegionPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<RegionPropertyId> validPropertyIds = regionDataManager.getRegionPropertyIds();
		for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
			if (!validPropertyIds.contains(regionPropertyId)) {
				throw new RuntimeException("invalid property id " + regionPropertyId);
			}
		}

		if (regionPropertyIds.equals(regionDataManager.getRegionPropertyIds())) {
			actorContext.subscribe(RegionPropertyUpdateEvent.class, this::handleRegionPropertyUpdateEvent);
		} else {
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				EventLabel<RegionPropertyUpdateEvent> eventLabelByProperty = RegionPropertyUpdateEvent.getEventLabelByProperty(actorContext, regionPropertyId);
				actorContext.subscribe(eventLabelByProperty, this::handleRegionPropertyUpdateEvent);
			}
		}

		for (final RegionId regionId : regionDataManager.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object propertyValue = regionDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(actorContext, regionId, regionPropertyId, propertyValue);
			}
		}

	}

	private void writeProperty(ActorContext actorContext, final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {

		
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);

		reportItemBuilder.addValue(actorContext.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		actorContext.releaseOutput(reportItemBuilder.build());
	}

}