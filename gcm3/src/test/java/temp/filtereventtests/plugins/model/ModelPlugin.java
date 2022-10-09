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

							ModelPluginData pluginData = c.getPluginData(ModelPluginData.class);
							c.addDataManager(new ModelDataManager(pluginData));
							int observerCount = pluginData.getObserverCount();
							for (int i = 0; i < observerCount; i++) {
								c.addActor(new PropertyObserver()::init);
							}
						})//
						.build();

	}

}
