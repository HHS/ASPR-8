package plugins.regions.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ReportContext;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.events.RegionPropertyDefinitionEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.*;
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

	private static class Data {
		private ReportLabel reportLabel;
		private Set<RegionPropertyId> includedRegionPropertyIds = new LinkedHashSet<>();
		private Set<RegionPropertyId> excludedRegionPropertyIds = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;
	}

	/**
	 * Returns a new instance of the builder class
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for the report
	 *
	 *
	 */
	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		public RegionPropertyReport build() {
			try {
				return new RegionPropertyReport(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the default policy for inclusion of region properties in the
		 * report. This policy is used when a region property has not been
		 * explicitly included or excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given region property id to be included in the report.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
		 *             person property id is null</li>
		 */
		public Builder includeRegionProperty(RegionPropertyId regionPropertyId) {
			if (regionPropertyId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
			data.includedRegionPropertyIds.add(regionPropertyId);
			data.excludedRegionPropertyIds.remove(regionPropertyId);
			return this;
		}

		/**
		 * Selects the given region property id to be excluded from the report
		 *
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
		 *             region property id is null</li>
		 */
		public Builder excludePersonProperty(RegionPropertyId regionPropertyId) {
			if (regionPropertyId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
			data.includedRegionPropertyIds.remove(regionPropertyId);
			data.excludedRegionPropertyIds.add(regionPropertyId);
			return this;
		}

		/**
		 * Sets the report label
		 *
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_LABEL} if the report
		 *             label is null</li>
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
			return this;
		}
	}

	private ReportHeader reportHeader;

	private final Data data;

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
		if (data.includedRegionPropertyIds.contains(regionPropertyId)) {
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private RegionsDataManager regionsDataManager;

	private RegionPropertyReport(Data data) {
		this.data = data;
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
		if (data.defaultInclusionPolicy) {
			data.includedRegionPropertyIds.addAll(regionsDataManager.getRegionPropertyIds());
			data.includedRegionPropertyIds.removeAll(data.excludedRegionPropertyIds);
			reportContext.subscribe(RegionPropertyDefinitionEvent.class, this::handleRegionPropertyDefinitionEvent);
		}

		reportContext.subscribe(RegionPropertyUpdateEvent.class, this::handleRegionPropertyUpdateEvent);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : data.includedRegionPropertyIds) {
				if (regionsDataManager.regionPropertyIdExists(regionPropertyId)) {
					Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
					writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
				}
			}
		}

		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionId regionId = regionAdditionEvent.getRegionId();

		for (final RegionPropertyId regionPropertyId : data.includedRegionPropertyIds) {
			if (regionsDataManager.regionPropertyIdExists(regionPropertyId)) {
				Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
			}
		}

	}

	private void handleRegionPropertyDefinitionEvent(ReportContext reportContext, RegionPropertyDefinitionEvent regionPropertyDefinitionEvent) {

		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		RegionPropertyId regionPropertyId = regionPropertyDefinitionEvent.regionPropertyId();
		data.includedRegionPropertyIds.add(regionPropertyId);
		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private void writeProperty(ReportContext reportContext, final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportLabel(data.reportLabel);

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}