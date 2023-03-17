package plugins.globalproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ReportContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
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
	private final Set<GlobalPropertyId> excludedPropertyIds = new LinkedHashSet<>();
	private final ReportLabel reportLabel;
	private final boolean includeNewPropertyIds;

	private final ReportHeader reportHeader = ReportHeader	.builder()//
															.add("time")//
															.add("property")//
															.add("value")//
															.build();//

	/**
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PROPERTY_REPORT_PLUGIN_DATA}
	 *             if the plugin data is null</li>
	 * 
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
		if (!excludedPropertyIds.contains(globalPropertyId)) {
			includedPropertyIds.add(globalPropertyId);
			writeProperty(reportContext, globalPropertyId, globalPropertyDefinitionEvent.initialPropertyValue());

		}
	}

	private void handleGlobalPropertyUpdateEvent(final ReportContext reportContext, final GlobalPropertyUpdateEvent globalPropertyUpdateEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyUpdateEvent.globalPropertyId();
		if (includedPropertyIds.contains(globalPropertyId)) {
			writeProperty(reportContext, globalPropertyId, globalPropertyUpdateEvent.currentPropertyValue());
		}
	}

	/**
	 * Initialization of the report.
	 */
	public void init(final ReportContext reportContext) {

		final GlobalPropertiesDataManager globalPropertiesDataManager = reportContext.getDataManager(GlobalPropertiesDataManager.class);

		/*
		 * if the client has selected all extant properties, then correct the
		 * data's included property ids
		 */
		if (includeNewPropertyIds) {
			includedPropertyIds.addAll(globalPropertiesDataManager.getGlobalPropertyIds());
			includedPropertyIds.removeAll(excludedPropertyIds);
			reportContext.subscribe(GlobalPropertyDefinitionEvent.class, this::handleGlobalPropertyDefinitionEvent);
		}

		/*
		 * We now subscribe to all update and definition events without any
		 * filtering
		 */
		reportContext.subscribe(GlobalPropertyUpdateEvent.class, this::handleGlobalPropertyUpdateEvent);

		/*
		 * We initialize the reporting with the current state of each global
		 * property
		 */
		for (final GlobalPropertyId globalPropertyId : includedPropertyIds) {

			if (globalPropertiesDataManager.globalPropertyIdExists(globalPropertyId)) {
				final Object globalPropertyValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
				writeProperty(reportContext, globalPropertyId, globalPropertyValue);
			}
		}

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