package lessons.lesson_12.plugins.model;

import nucleus.Plugin;

public class ModelPlugin {

	private ModelPlugin() {
	}

	public static Plugin getModelPlugin() {

		return Plugin	.builder()//
						.setPluginId(ModelPluginId.PLUGIN_ID)//
						.setInitializer((c) -> {
							c.addActor(new PopulationLoader()::init);
							c.addActor(new VaccineScheduler()::init);
						})//
						.build();
	}
}
