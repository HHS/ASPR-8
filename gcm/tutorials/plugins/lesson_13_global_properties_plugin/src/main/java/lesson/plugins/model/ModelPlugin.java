package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import lesson.plugins.model.actors.GammaActor;

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
