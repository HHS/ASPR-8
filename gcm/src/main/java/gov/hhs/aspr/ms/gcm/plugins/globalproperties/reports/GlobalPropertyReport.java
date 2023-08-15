package gov.hhs.aspr.ms.gcm.plugins.globalproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertiesError;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import util.errors.ContractException;

/**
 * A Report that displays assigned global property values over time.
 *
 * Fields
 *
 * Time -- the time in days when the global property was set
 *
 * Property -- the global property identifier
 *
 * Value -- the value of the global property
 *
 *
 */
public final class GlobalPropertyReport {

	private final Set<GlobalPropertyId> includedPropertyIds = new LinkedHashSet<>();
	private final Set<GlobalPropertyId> currentProperties = new LinkedHashSet<>();
	private final Set<GlobalPropertyId> excludedPropertyIds = new LinkedHashSet<>();
	private final ReportLabel reportLabel;
	private final boolean includeNewPropertyIds;

	private final ReportHeader reportHeader = ReportHeader	.builder()//
															.add("time")//
															.add("property")//
															.add("value")//
															.build();//

	private boolean isCurrentProperty(GlobalPropertyId globalPropertyId) {
		return currentProperties.contains(globalPropertyId);
	}

	private boolean addToCurrentProperties(GlobalPropertyId globalPropertyId) {

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
		if (excludedPropertyIds.contains(globalPropertyId)) {
			return false;
		}

		// if both P and I are false we don't add the property
		boolean included = includedPropertyIds.contains(globalPropertyId);

		if (!included && !includeNewPropertyIds) {
			return false;
		}

		// we have failed to reject the property
		currentProperties.add(globalPropertyId);

		return true;
	}

	/**
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA}
	 *             if the plugin data is null</li>
	 */
	public GlobalPropertyReport(GlobalPropertyReportPluginData globalPropertyReportPluginData) {

		if (globalPropertyReportPluginData == null) {
			throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA);
		}

		reportLabel = globalPropertyReportPluginData.getReportLabel();
		includedPropertyIds.addAll(globalPropertyReportPluginData.getIncludedProperties());
		excludedPropertyIds.addAll(globalPropertyReportPluginData.getExcludedProperties());
		includeNewPropertyIds = globalPropertyReportPluginData.getDefaultInclusionPolicy();
	}

	private void handleGlobalPropertyDefinitionEvent(final ReportContext reportContext, final GlobalPropertyDefinitionEvent globalPropertyDefinitionEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyDefinitionEvent.globalPropertyId();
		if (addToCurrentProperties(globalPropertyId)) {
			writeProperty(reportContext, globalPropertyId, globalPropertyDefinitionEvent.initialPropertyValue());
		}
	}

	private void handleGlobalPropertyUpdateEvent(final ReportContext reportContext, final GlobalPropertyUpdateEvent globalPropertyUpdateEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyUpdateEvent.globalPropertyId();
		if (isCurrentProperty(globalPropertyId)) {
			writeProperty(reportContext, globalPropertyId, globalPropertyUpdateEvent.currentPropertyValue());
		}
	}

	/**
	 * Initialization of the report.
	 */
	public void init(final ReportContext reportContext) {

		final GlobalPropertiesDataManager globalPropertiesDataManager = reportContext.getDataManager(GlobalPropertiesDataManager.class);

		reportContext.subscribe(GlobalPropertyDefinitionEvent.class, this::handleGlobalPropertyDefinitionEvent);
		reportContext.subscribe(GlobalPropertyUpdateEvent.class, this::handleGlobalPropertyUpdateEvent);
		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		for (GlobalPropertyId globalPropertyId : globalPropertiesDataManager.getGlobalPropertyIds()) {
			addToCurrentProperties(globalPropertyId);
		}

		/*
		 * We initialize the reporting with the current state of each global
		 * property
		 */
		for (final GlobalPropertyId globalPropertyId : currentProperties) {
			final Object globalPropertyValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			writeProperty(reportContext, globalPropertyId, globalPropertyValue);
		}

	}

	private void recordSimulationState(ReportContext reportContext) {
		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		for (GlobalPropertyId globalPropertyId : includedPropertyIds) {
			builder.includeGlobalProperty(globalPropertyId);
		}
		for (GlobalPropertyId globalPropertyId : excludedPropertyIds) {
			builder.excludeGlobalProperty(globalPropertyId);
		}
		builder.setDefaultInclusion(includeNewPropertyIds);
		reportContext.releaseOutput(builder.build());
	}

	private void writeProperty(final ReportContext reportContext, final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(reportHeader);
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(globalPropertyId.toString());
		reportItemBuilder.addValue(globalPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}