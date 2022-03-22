package plugins.regions;

import nucleus.Plugin;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.reports.ReportsPluginId;

public final class RegionPlugin {

	private RegionPlugin() {
	}

	public static Plugin getRegionPlugin(RegionPluginData regionPluginData) {

		return Plugin	.builder()//
						.addPluginData(regionPluginData)//
						.setPluginId(RegionPluginId.PLUGIN_ID)//
						.addPluginDependency(PartitionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							RegionPluginData pluginData = c.getPluginData(RegionPluginData.class).get();
							c.addDataManager(new RegionDataManager(pluginData));
						})//
						.build();

	}

}
