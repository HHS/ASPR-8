package lessons.lesson_11.plugins.model;

import nucleus.Plugin;

public final class ModelPlugin {

	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {

		return Plugin	.builder()//
						.setPluginId(ModelPluginId.PLUGIN_ID)//						
						.setInitializer((c) -> {
							c.addActor(new ModelActor()::init);
						})//
						.build();
	}

}
