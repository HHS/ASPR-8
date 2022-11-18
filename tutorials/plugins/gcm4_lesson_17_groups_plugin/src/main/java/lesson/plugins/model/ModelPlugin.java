package lesson.plugins.model;

import lesson.plugins.model.actors.InfectionManager;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.TeleworkManager;
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
							c.addActor(new InfectionManager()::init);
							c.addActor(new TeleworkManager()::init);
							
						}).build();
	}
}
