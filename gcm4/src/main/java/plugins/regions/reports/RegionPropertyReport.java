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
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
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

	private final Set<RegionPropertyId> includedPropertyIds = new LinkedHashSet<>();
	private final Set<RegionPropertyId> currentProperties = new LinkedHashSet<>();
	private final Set<RegionPropertyId> excludedPropertyIds = new LinkedHashSet<>();
	private final ReportLabel reportLabel;
	private final boolean includeNewPropertyIds;

	private final ReportHeader reportHeader = ReportHeader	.builder()//
															.add("Time")//
															.add("Region")//
															.add("Property")//
															.add("Value")//
															.build();//

	private boolean isCurrentProperty(RegionPropertyId regionPropertyId) {
		return currentProperties.contains(regionPropertyId);
	}

	private boolean addToCurrentProperties(RegionPropertyId regionPropertyId) {

		// There are eight possibilities:

		/*
		 * P -- the default inclusion policy
		 *
		 * I -- the property is explicitly included
		 *
		 * X -- the property is explicitly excluded
		 *
		 * C -- the property should be on the current properties
		 *
		 *
		 * P I X C Table
		 *
		 * TRUE TRUE FALSE TRUE
		 *
		 * TRUE FALSE FALSE TRUE
		 *
		 * FALSE TRUE FALSE TRUE
		 *
		 * FALSE FALSE FALSE FALSE
		 *
		 * TRUE TRUE TRUE FALSE -- not possible
		 *
		 * TRUE FALSE TRUE FALSE
		 *
		 * FALSE TRUE TRUE FALSE -- not possible
		 *
		 * FALSE FALSE TRUE FALSE
		 *
		 *
		 * Two of the cases above are contradictory since a property cannot be
		 * both explicitly included and explicitly excluded
		 *
		 */

		// if X is true then we don't add the property
		if (excludedPropertyIds.contains(regionPropertyId)) {
			return false;
		}

		// if both P and I are false we don't add the property
		boolean included = includedPropertyIds.contains(regionPropertyId);

		if (!included && !includeNewPropertyIds) {
			return false;
		}

		// we have failed to reject the property
		currentProperties.add(regionPropertyId);

		return true;
	}

	/**
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA}
	 *             if the plugin data is null</li>
	 *
	 */
	public RegionPropertyReport(RegionPropertyReportPluginData regionPropertyReportPluginData) {
		if (regionPropertyReportPluginData == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_REPORT_PLUGIN_DATA);
		}

		reportLabel = regionPropertyReportPluginData.getReportLabel();
		includedPropertyIds.addAll(regionPropertyReportPluginData.getIncludedProperties());
		excludedPropertyIds.addAll(regionPropertyReportPluginData.getExcludedProperties());
		includeNewPropertyIds = regionPropertyReportPluginData.getDefaultInclusionPolicy();
	}

	private void handleRegionPropertyUpdateEvent(ReportContext reportContext, RegionPropertyUpdateEvent regionPropertyUpdateEvent) {
		RegionPropertyId regionPropertyId = regionPropertyUpdateEvent.regionPropertyId();
		if (isCurrentProperty(regionPropertyId)) {
			RegionId regionId = regionPropertyUpdateEvent.regionId();
			Object propertyValue = regionPropertyUpdateEvent.currentPropertyValue();
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private void handleRegionAdditionEvent(ReportContext reportContext, RegionAdditionEvent regionAdditionEvent) {
		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		RegionId regionId = regionAdditionEvent.getRegionId();

		for (final RegionPropertyId regionPropertyId : currentProperties) {
			Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
			writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
		}
	}

	private void handleRegionPropertyDefinitionEvent(ReportContext reportContext, RegionPropertyDefinitionEvent regionPropertyDefinitionEvent) {
		RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);
		RegionPropertyId regionPropertyId = regionPropertyDefinitionEvent.regionPropertyId();
		if (addToCurrentProperties(regionPropertyId)) {
			for (final RegionId regionId : regionsDataManager.getRegionIds()) {
				Object propertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId, propertyValue);
			}
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
		final RegionsDataManager regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		reportContext.subscribe(RegionPropertyDefinitionEvent.class, this::handleRegionPropertyDefinitionEvent);
		reportContext.subscribe(RegionPropertyUpdateEvent.class, this::handleRegionPropertyUpdateEvent);
		reportContext.subscribe(RegionAdditionEvent.class, this::handleRegionAdditionEvent);
		if (reportContext.produceSimulationStateOnHalt()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		for (RegionPropertyId regionPropertyId : regionsDataManager.getRegionPropertyIds()) {
			addToCurrentProperties(regionPropertyId);
		}

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : currentProperties) {
				final Object regionPropertyValue = regionsDataManager.getRegionPropertyValue(regionId, regionPropertyId);
				writeProperty(reportContext, regionId, regionPropertyId, regionPropertyValue);
			}
		}
	}

	private void recordSimulationState(ReportContext reportContext) {
		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		for (RegionPropertyId regionPropertyId : includedPropertyIds) {
			builder.includeRegionProperty(regionPropertyId);
		}
		for (RegionPropertyId regionPropertyId : excludedPropertyIds) {
			builder.excludeRegionProperty(regionPropertyId);
		}
		builder.setDefaultInclusion(includeNewPropertyIds);
		
		reportContext.releaseOutput(builder.build());
	}

	private void writeProperty(ReportContext reportContext, final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(reportHeader);
		reportItemBuilder.setReportLabel(reportLabel);

		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(regionId.toString());
		reportItemBuilder.addValue(regionPropertyId.toString());
		reportItemBuilder.addValue(regionPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}