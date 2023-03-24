package lesson.plugins.model;

import lesson.plugins.model.actors.PersonMover;
import lesson.plugins.model.actors.RegionCreator;
import lesson.plugins.model.actors.Vaccinator;
import nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin	.builder()//						
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
							c.addActor(new PersonMover()::init);
							c.addActor(new Vaccinator()::init);							
							c.addActor(new RegionCreator()::init);
						}).build();
	}
}
