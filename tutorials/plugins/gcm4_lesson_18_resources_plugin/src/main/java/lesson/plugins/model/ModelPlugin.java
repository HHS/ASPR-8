package lesson.plugins.model;

import lesson.plugins.model.actors.TreatmentManager;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.ResourceLoader;
import nucleus.Plugin;
import plugins.reports.ReportsPluginId;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin	.builder()//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
									c.addActor(new PopulationLoader()::init);
									c.addActor(new ResourceLoader()::init);
									c.addActor(new TreatmentManager()::init);
									
						}).build();
	}
}
