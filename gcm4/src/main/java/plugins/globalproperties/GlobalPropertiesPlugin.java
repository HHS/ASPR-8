package plugins.globalproperties;

import java.util.Optional;

import nucleus.Plugin;
import nucleus.PluginContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.globalproperties.reports.GlobalPropertyReport;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertiesError;
import util.errors.ContractException;

/**
 * A plugin providing a global property data manager to the simulation.
 * 
 *
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
		 *
		 * <P>
		 * Uses GlobalsPluginId.PLUGIN_ID as its id
		 * </P>
		 * 
		 * <P>
		 * Depends on plugins:
		 * <ul>
		 * <li>Report Plugin</li>
		 * </ul>
		 * </P>
		 * 
		 * <P>
		 * Provides data mangers:
		 * <ul>
		 * <li>{@linkplain GlobalPropertiesDataManager}</li>
		 * </ul>
		 * </P>
		 * 
		 * <P>
		 * Provides reports:
		 * <ul>
		 * <li>{@linkplain GlobalPropertyReport}</li>
		 * </ul>
		 * </P>
		 * 
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GlobalPropertiesError#NULL_GLOBAL_PLUGIN_DATA}
		 *             if the global plugin data is null</li>
		 * 
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

		public Builder setGlobalPropertyReportPluginData(GlobalPropertyReportPluginData globalPropertyReportPluginData) {
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

		Optional<GlobalPropertyReportPluginData> optional = pluginContext.getPluginData(GlobalPropertyReportPluginData.class);
		if (optional.isPresent()) {
			GlobalPropertyReportPluginData globalPropertyReportPluginData = optional.get();
			pluginContext.addReport(new GlobalPropertyReport(globalPropertyReportPluginData)::init);
		}
	}

}
