package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.reports.GlobalPropertyReport;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertiesError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A plugin providing a global property data manager to the simulation.
 */
public final class GlobalPropertiesPlugin {

	private static class Data {
		private GlobalPropertyReportPluginData globalPropertyReportPluginData;
		private GlobalPropertiesPluginData globalPropertiesPluginData;
	}

	private GlobalPropertiesPlugin() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		/**
		 * Returns the global plugin.
		 * <p>
		 * Uses GlobalsPluginId.PLUGIN_ID as its id
		 * </p>
		 * <p>
		 * Depends on plugins:
		 * <ul>
		 * <li>Report Plugin</li>
		 * </ul>
		 * <p>
		 * Provides data mangers:
		 * <ul>
		 * <li>{@linkplain GlobalPropertiesDataManager}</li>
		 * </ul>
		 * <p>
		 * Provides reports:
		 * <ul>
		 * <li>{@linkplain GlobalPropertyReport}</li>
		 * </ul>
		 * 
		 * @throws ContractException {@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
		 *                           if the global plugin data is null
		 */
		public Plugin getGlobalPropertiesPlugin() {
			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.addPluginData(data.globalPropertiesPluginData);//

			if (data.globalPropertyReportPluginData != null) {
				builder.addPluginData(data.globalPropertyReportPluginData);
			}
			builder.setInitializer(GlobalPropertiesPlugin::init);
			builder.setPluginId(GlobalPropertiesPluginId.PLUGIN_ID);//
			return builder.build();
		}

		private void validate() {
			if (data.globalPropertiesPluginData == null) {
				throw new ContractException(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA);
			}
		}

		public Builder setGlobalPropertyReportPluginData(
				GlobalPropertyReportPluginData globalPropertyReportPluginData) {
			data.globalPropertyReportPluginData = globalPropertyReportPluginData;
			return this;
		}

		public Builder setGlobalPropertiesPluginData(GlobalPropertiesPluginData globalPropertiesPluginData) {
			data.globalPropertiesPluginData = globalPropertiesPluginData;
			return this;
		}

	}

	private static void init(PluginContext pluginContext) {
		GlobalPropertiesPluginData data = pluginContext.getPluginData(GlobalPropertiesPluginData.class).get();
		pluginContext.addDataManager(new GlobalPropertiesDataManager(data));

		Optional<GlobalPropertyReportPluginData> optional = pluginContext
				.getPluginData(GlobalPropertyReportPluginData.class);
		if (optional.isPresent()) {
			GlobalPropertyReportPluginData globalPropertyReportPluginData = optional.get();
			pluginContext.addReport(new GlobalPropertyReport(globalPropertyReportPluginData)::init);
		}
	}

}
