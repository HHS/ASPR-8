package gov.hhs.aspr.ms.gcm.plugins.regions;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionPropertyReport;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionTransferReport;
import gov.hhs.aspr.ms.gcm.plugins.regions.reports.RegionTransferReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import util.errors.ContractException;

public final class RegionsPlugin {

	private RegionsPlugin() {
	}

	private static class Data {
		private RegionsPluginData regionsPluginData;
		private RegionPropertyReportPluginData regionPropertyReportPluginData;
		private RegionTransferReportPluginData regionTransferReportPluginData;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.regionsPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
			}
		}

		/**
		 * Builds the RegionsPlugin from the collected inputs
		 * 
		 * @throws ContractException {@linkplain RegionError#NULL_REGION_PLUGIN_DATA} if
		 *                           the regionsPluginData is null
		 */
		public Plugin getRegionsPlugin() {

			validate();
			Plugin.Builder builder = Plugin.builder();//
			builder.setPluginId(RegionsPluginId.PLUGIN_ID);//
			builder.addPluginData(data.regionsPluginData);//
			if (data.regionPropertyReportPluginData != null) {
				builder.addPluginData(data.regionPropertyReportPluginData);//
			}
			if (data.regionTransferReportPluginData != null) {
				builder.addPluginData(data.regionTransferReportPluginData);//
			}
			builder.addPluginDependency(PeoplePluginId.PLUGIN_ID);//

			builder.setInitializer((c) -> {
				RegionsPluginData pluginData = c.getPluginData(RegionsPluginData.class).get();
				c.addDataManager(new RegionsDataManager(pluginData));

				Optional<RegionPropertyReportPluginData> optional1 = c
						.getPluginData(RegionPropertyReportPluginData.class);
				if (optional1.isPresent()) {
					RegionPropertyReportPluginData regionPropertyReportPluginData = optional1.get();
					c.addReport(new RegionPropertyReport(regionPropertyReportPluginData)::init);
				}

				Optional<RegionTransferReportPluginData> optional2 = c
						.getPluginData(RegionTransferReportPluginData.class);
				if (optional2.isPresent()) {
					RegionTransferReportPluginData regionTransferReportPluginData = optional2.get();
					c.addReport(new RegionTransferReport(regionTransferReportPluginData)::init);
				}

			});
			return builder.build();

		}

		public Builder setRegionsPluginData(RegionsPluginData regionsPluginData) {
			data.regionsPluginData = regionsPluginData;
			return this;
		}

		public Builder setRegionPropertyReportPluginData(
				RegionPropertyReportPluginData regionPropertyReportPluginData) {
			data.regionPropertyReportPluginData = regionPropertyReportPluginData;
			return this;
		}

		public Builder setRegionTransferReportPluginData(
				RegionTransferReportPluginData regionTransferReportPluginData) {
			data.regionTransferReportPluginData = regionTransferReportPluginData;
			return this;
		}
	}

	// public static Plugin getRegionsPlugin(RegionsPluginData
	// regionsPluginData) {
	//
	// return Plugin .builder()//

	//
	//
	// .setInitializer((c) -> {
	// RegionsPluginData pluginData =
	// c.getPluginData(RegionsPluginData.class).get();
	// c.addDataManager(new RegionsDataManager(pluginData));
	//
	//
	// })//
	// .build();
	//
	// }

}
