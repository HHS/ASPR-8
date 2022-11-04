package lesson.plugins.model;

import lesson.plugins.model.actors.PersonMover;
import lesson.plugins.model.actors.RegionCreator;
import lesson.plugins.model.actors.Vaccinator;
import nucleus.Plugin;
import plugins.reports.ReportsPluginId;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin	.builder()//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
							c.addActor(new PersonMover()::init);
							c.addActor(new Vaccinator()::init);							
							c.addActor(new RegionCreator()::init);
						}).build();
	}
}
