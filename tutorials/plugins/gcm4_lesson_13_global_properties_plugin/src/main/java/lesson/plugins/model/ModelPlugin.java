package lesson.plugins.model;

import lesson.plugins.model.actors.GammaActor;
import nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new GammaActor()::init);
				}).build();
	}
}
