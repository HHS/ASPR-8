package plugins.resources;

import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.regions.RegionsPluginId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.dataviews.ResourcesDataView;

public final class ResourcesPlugin {

	private ResourcesPlugin() {
	}

	public static Plugin getResourcesPlugin(ResourcesPluginData resourcesPluginData) {

		return Plugin	.builder()//
						.setPluginId(ResourcesPluginId.PLUGIN_ID)//
						.addPluginData(resourcesPluginData)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.addPluginDependency(RegionsPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							ResourcesPluginData pluginData = c.getPluginData(ResourcesPluginData.class);
							ResourcesDataManager resourcesDataManager = new ResourcesDataManager(pluginData);
							c.addDataManager(resourcesDataManager);
							ResourcesDataView resourcesDataView = new ResourcesDataView(resourcesDataManager);
							c.addDataView(resourcesDataView);
						})//
						.build();
	}

}
