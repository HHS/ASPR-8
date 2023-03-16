package plugins.regions;

import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.regions.datamanagers.RegionsDataManager;

public final class RegionsPlugin {

	private RegionsPlugin() {
	}

	public static Plugin getRegionsPlugin(RegionsPluginData regionsPluginData) {

		return Plugin	.builder()//
						.addPluginData(regionsPluginData)//
						.setPluginId(RegionsPluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							RegionsPluginData pluginData = c.getPluginData(RegionsPluginData.class).get();
							c.addDataManager(new RegionsDataManager(pluginData));
						})//
						.build();

	}

}
