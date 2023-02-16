package plugins.globalproperties.actors;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ReportContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.util.properties.PropertyError;
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

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for the global property report
	 *
	 *
	 */
	public final static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
		}

		/**
		 * Returns the global property report from the collected data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_LABEL} if the report
		 *             label was not set</li>
		 */
		public GlobalPropertyReport build() {
			try {
				validate();
				return new GlobalPropertyReport(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Includes a global property in this report. These property ids do not
		 * have to be valid id values that exist in the simulation.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             global property id is null</li>
		 */
		public Builder includePropertyId(GlobalPropertyId globalPropertyId) {
			if (globalPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedPropertyIds.add(globalPropertyId);
			data.excludedPropertyIds.remove(globalPropertyId);
			return this;
		}

		/**
		 * Includes all extant global property ids at the initialization of this
		 * report, excepting those that have been explicitly excluded. Defaults
		 * to false.
		 * 
		 */
		public Builder includeAllExtantPropertyIds(boolean includeAllExtantPropertyIds) {
			data.includeAllExtantPropertyIds = includeAllExtantPropertyIds;
			return this;
		}

		/**
		 * Excludes a global property from this report. These property ids do
		 * not have to be valid id values that exist in the simulation.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             global property id is null</li>
		 * 
		 */
		public Builder excludePropertyId(GlobalPropertyId globalPropertyId) {
			if (globalPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedPropertyIds.remove(globalPropertyId);
			data.excludedPropertyIds.add(globalPropertyId);
			return this;
		}

		/**
		 * Sets the report label. Defaults to null.
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

		/**
		 * Forces the inclusion of new properties that are not already selected
		 * for exclusion. Defaults to false.
		 */
		public Builder includeNewPropertyIds(boolean includeNewPropertyIds) {
			data.includeNewPropertyIds = includeNewPropertyIds;
			return this;
		}

	}

	private static class Data {
		private final Set<GlobalPropertyId> includedPropertyIds = new LinkedHashSet<>();
		private final Set<GlobalPropertyId> excludedPropertyIds = new LinkedHashSet<>();
		private ReportLabel reportLabel;
		private boolean includeNewPropertyIds;
		private boolean includeAllExtantPropertyIds;
		private final ReportHeader reportHeader = ReportHeader	.builder()//
																.add("time")//
																.add("property")//
																.add("value")//
																.build();//
	}

	private final Data data;

	private GlobalPropertyReport(final Data data) {
		this.data = data;
	}

	private void handleGlobalPropertyDefinitionEvent(final ReportContext reportContext, final GlobalPropertyDefinitionEvent globalPropertyDefinitionEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyDefinitionEvent.globalPropertyId();
		/*
		 * If the property id is explicitly excluded, then ignore it
		 */
		if (data.excludedPropertyIds.contains(globalPropertyId)) {
			return;
		}

		boolean included = data.includedPropertyIds.contains(globalPropertyId);
		if (!included && data.includeNewPropertyIds) {
			data.includedPropertyIds.add(globalPropertyId);
			included = true;
		}

		if (included) {
			writeProperty(reportContext, globalPropertyId, globalPropertyDefinitionEvent.initialPropertyValue());
		}
	}

	private void handleGlobalPropertyUpdateEvent(final ReportContext reportContext, final GlobalPropertyUpdateEvent globalPropertyUpdateEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyUpdateEvent.globalPropertyId();
		if (data.includedPropertyIds.contains(globalPropertyId)) {
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
		if (data.includeAllExtantPropertyIds) {
			data.includedPropertyIds.addAll(globalPropertiesDataManager.getGlobalPropertyIds());
		}

		/*
		 * if there are no property ids selected and we will not be including
		 * new added ids, then there is no point in subscribing
		 */
		if (data.includedPropertyIds.isEmpty() && !data.includeNewPropertyIds) {
			return;
		}

		/*
		 * We now subscribe to all update and definition events without any
		 * filtering
		 */
		reportContext.subscribe(GlobalPropertyUpdateEvent.class, this::handleGlobalPropertyUpdateEvent);
		reportContext.subscribe(GlobalPropertyDefinitionEvent.class, this::handleGlobalPropertyDefinitionEvent);

		/*
		 * We initialize the reporting with the current state of each global
		 * property
		 */
		for (final GlobalPropertyId globalPropertyId : data.includedPropertyIds) {
			final Object globalPropertyValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			writeProperty(reportContext, globalPropertyId, globalPropertyValue);
		}

	}

	private void writeProperty(final ReportContext reportContext, final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(data.reportHeader);
		reportItemBuilder.setReportLabel(data.reportLabel);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(globalPropertyId.toString());
		reportItemBuilder.addValue(globalPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}