package plugins.globalproperties.actors;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
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
 * @author Shawn Hatch
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
	 * @author Shawn Hatch
	 *
	 */
	public final static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}
		}

		/**
		 * Returns the global property report from the collected data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the report
		 *             id was not set</li>
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
			data.includedPropertyIds.remove(globalPropertyId);
			data.excludedPropertyIds.add(globalPropertyId);
			return this;
		}

		/**
		 * Sets the report id. Defaults to null.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the report
		 *             id is null</li>
		 */
		public Builder setReportId(ReportId reportId) {
			if (reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}
			data.reportId = reportId;
			return this;
		}

		/**
		 * Forces the inclusion of new properties that are not already selected
		 * for exclusion. Defaults to false.
		 */
		public Builder includeNewPropertyIds(boolean includeNewProperties) {
			data.includeNewPropertyIds = includeNewProperties;
			return this;
		}

	}

	private static class Data {
		private final Set<GlobalPropertyId> includedPropertyIds = new LinkedHashSet<>();
		private final Set<GlobalPropertyId> excludedPropertyIds = new LinkedHashSet<>();
		private ReportId reportId;
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

	private void handleGlobalPropertyDefinitionEvent(final ActorContext actorContext, final GlobalPropertyDefinitionEvent globalPropertyDefinitionEvent) {
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
			writeProperty(actorContext, globalPropertyId, globalPropertyDefinitionEvent.initialPropertyValue());
		}
	}

	private void handleGlobalPropertyUpdateEvent(final ActorContext actorContext, final GlobalPropertyUpdateEvent globalPropertyUpdateEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyUpdateEvent.getGlobalPropertyId();
		if (data.includedPropertyIds.contains(globalPropertyId)) {
			writeProperty(actorContext, globalPropertyId, globalPropertyUpdateEvent.getCurrentPropertyValue());
		}
	}

	/**
	 * Initialization of the report.
	 */
	public void init(final ActorContext actorContext) {

		final GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

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
		actorContext.subscribe(GlobalPropertyUpdateEvent.class, this::handleGlobalPropertyUpdateEvent);
		actorContext.subscribe(GlobalPropertyDefinitionEvent.class, this::handleGlobalPropertyDefinitionEvent);

		/*
		 * We initialize the reporting with the current state of each global
		 * property
		 */
		for (final GlobalPropertyId globalPropertyId : data.includedPropertyIds) {
			final Object globalPropertyValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			writeProperty(actorContext, globalPropertyId, globalPropertyValue);
		}

	}

	private void writeProperty(final ActorContext actorContext, final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(data.reportHeader);
		reportItemBuilder.setReportId(data.reportId);
		reportItemBuilder.addValue(actorContext.getTime());
		reportItemBuilder.addValue(globalPropertyId.toString());
		reportItemBuilder.addValue(globalPropertyValue);
		actorContext.releaseOutput(reportItemBuilder.build());
	}

}