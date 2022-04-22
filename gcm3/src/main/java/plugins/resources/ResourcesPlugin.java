package plugins.resources;

import nucleus.Plugin;
import plugins.partitions.PartitionsPluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionPluginId;
import plugins.reports.ReportsPluginId;
import plugins.resources.datamanagers.ResourceDataManager;

public final class ResourcesPlugin {

	private ResourcesPlugin() {
	}

	public static Plugin getResourcesPlugin(ResourcesPluginData resourcesPluginData) {

		return Plugin	.builder()//
						.setPluginId(ResourcesPluginId.PLUGIN_ID)//
						.addPluginData(resourcesPluginData)//
						.addPluginDependency(PartitionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.addPluginDependency(RegionPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							ResourcesPluginData pluginData = c.getPluginData(ResourcesPluginData.class);
							c.addDataManager(new ResourceDataManager(pluginData));
						})//
						.build();
	}

}
