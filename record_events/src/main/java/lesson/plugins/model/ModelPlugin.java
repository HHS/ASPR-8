package lesson.plugins.model;

import lesson.plugins.model.actors.Manipulator;
import lesson.plugins.model.actors.PopulationLoader;
import nucleus.Plugin;
import plugins.reports.ReportsPluginId;

public final class ModelPlugin {
	public static Plugin getModelPlugin() {
		return Plugin	.builder()//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
							c.addActor(new PopulationLoader()::init);							
							c.addActor(new Manipulator()::init);
						}).build();
	}

	private ModelPlugin() {

	}
}
