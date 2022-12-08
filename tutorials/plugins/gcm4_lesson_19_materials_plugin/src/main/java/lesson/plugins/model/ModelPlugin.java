package lesson.plugins.model;

import lesson.plugins.model.actors.ContactManager;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.ResourceLoader;
import lesson.plugins.model.actors.Vaccinator;
import nucleus.Plugin;
import plugins.reports.ReportsPluginId;

public final class ModelPlugin {
	public static Plugin getModelPlugin() {
		return Plugin	.builder()//
						.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
							c.addActor(new PopulationLoader()::init);
							c.addActor(new ResourceLoader()::init);
							c.addActor(new ContactManager()::init);
							c.addActor(new Vaccinator()::init);
						}).build();
	}

	private ModelPlugin() {

	}
}
