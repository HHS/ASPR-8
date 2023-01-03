package plugins.materials;

import nucleus.Plugin;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.regions.RegionsPluginId;
import plugins.resources.ResourcesPluginId;

/**
 * A plugin providing a materials data manager to the simulation.
 * 
 *
 */
public final class MaterialsPlugin {
	private MaterialsPlugin() {
	}

	public static Plugin getMaterialsPlugin(MaterialsPluginData materialsPluginData) {

		return Plugin	.builder()//
						.setPluginId(MaterialsPluginId.PLUGIN_ID)//
						.addPluginData(materialsPluginData)//
						.addPluginDependency(RegionsPluginId.PLUGIN_ID)//
						.addPluginDependency(ResourcesPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							MaterialsPluginData pluginData = c.getPluginData(MaterialsPluginData.class);
							c.addDataManager(new MaterialsDataManager(pluginData));
						}).build();

	}

}
