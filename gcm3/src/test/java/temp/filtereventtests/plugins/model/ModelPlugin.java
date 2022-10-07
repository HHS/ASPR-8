package temp.filtereventtests.plugins.model;

import nucleus.Plugin;

public final class ModelPlugin {

	private ModelPlugin() {
	}

	public static Plugin getModelPlugin(ModelPluginData modelPluginData) {
		return Plugin	.builder()//
						.addPluginData(modelPluginData)//
						.setPluginId(ModelPluginId.PLUGIN_ID)//
						.setInitializer(c -> {
							c.addActor(new PropertyChanger()::init);
							c.addActor(new PropertyObserver()::init);
							ModelPluginData pluginData = c.getPluginData(ModelPluginData.class);
							c.addDataManager(new ModelDataManager(pluginData));
						})//
						.build();

	}

}
