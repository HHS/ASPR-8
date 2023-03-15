package lesson.plugins.model;

import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.Vaccinator;
import lesson.plugins.model.actors.VaccineEducator;
import nucleus.Plugin;
import plugins.reports.ReportsPluginId;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin	.builder()//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
							c.addActor(new VaccineEducator()::init);
							c.addActor(new Vaccinator()::init);
							c.addActor(new PopulationLoader()::init);							
						}).build();
	}
}
