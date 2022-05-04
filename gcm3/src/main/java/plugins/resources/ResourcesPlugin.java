package plugins.resources;

import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionPluginId;
import plugins.resources.datamanagers.ResourcesDataManager;

public final class ResourcesPlugin {

	private ResourcesPlugin() {
	}

	public static Plugin getResourcesPlugin(ResourcesPluginData resourcesPluginData) {

		return Plugin	.builder()//
						.setPluginId(ResourcesPluginId.PLUGIN_ID)//
						.addPluginData(resourcesPluginData)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(RegionPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							ResourcesPluginData pluginData = c.getPluginData(ResourcesPluginData.class);
							c.addDataManager(new ResourcesDataManager(pluginData));
						})//
						.build();
	}

}
