package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

public final class ModelPlugin {

	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {

		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addActor(new ModelActor()::init);
				})//
				.build();
	}

}
