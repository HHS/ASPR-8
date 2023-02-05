package plugins.regions.actors;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ReportContext;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.events.RegionPropertyDefinitionEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

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

	private void handleRegionPropertyUpdateEvent(ReportContext reportContext, RegionPropertyUpdateEvent regionPropertyUpdateEvent) {
		RegionId regionId = regionPropertyUpdateEvent.regionId();
		RegionPropertyId regionPropertyId = regionPropertyUpdateEvent.regionPropertyId();
		Object propertyValue = regionPropertyUpdateEvent.currentPropertyValue();
		if (regionPropertyIds.contains(regionPropertyId)) {
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private final ReportLabel reportLabel;

	private RegionsDataManager regionsDataManager;

	public RegionPropertyReport(ReportLabel reportLabel, RegionPropertyId... regionPropertyIds) {
		this.reportLabel = reportLabel;
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			this.regionPropertyIds.add(regionPropertyId);
		}
	}

	/**
	 * Initial behavior for this report. The report subscribes to
	 * {@linkplain RegionPropertyUpdateEvent} and releases a {@link ReportItem}
	 * for each region property's initial value.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
	 *             region property id used in the constructor is unknown</li>
	 */
	public void init(final ReportContext reportContext) {
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		/*
		 * If no region properties were specified, then assume all are wanted
		 */
		if (regionPropertyIds.size() == 0) {
			regionPropertyIds.addAll(regionsDataManager.getRegionPropertyIds());
		}

		/*
		 * Ensure that every client supplied property identifier is valid
		 */
		final Set<RegionPropertyId> validPropertyIds = regionsDataManager.getRegionPropertyIds();
		for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
			if (!validPropertyIds.contains(regionPropertyId)) {
				throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, regionPropertyId);
			}
		}

		reportContext.subscribe(RegionPropertyUpdateEvent.class, this::handleRegionPropertyUpdateEvent);
		reportContext.subscribe(RegionPropertyDefinitionEvent.class, this::handleRegionPropertyDefinitionEvent);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
			}
		}

		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		for (final RegionPropertyId regionPropertyId : regionPropertyIds) {
			Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}

	}

	private void handleRegionPropertyDefinitionEvent(ReportContext reportContext, RegionPropertyDefinitionEvent regionPropertyDefinitionEvent) {

		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		RegionPropertyId regionPropertyId = regionPropertyDefinitionEvent.regionPropertyId();
		if (!regionPropertyIds.contains(regionPropertyId)) {
			regionPropertyIds.add(regionPropertyId);
			for (final RegionId regionId : regionsDataManager.getRegionIds()) {
				Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
			}
		}
	}

	private void writeProperty(ReportContext reportContext, final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportLabel);

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}