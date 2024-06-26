package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PersonMover;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.RegionCreator;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.Vaccinator;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new PersonMover()::init);
					c.addActor(new Vaccinator()::init);
					c.addActor(new RegionCreator()::init);
				}).build();
	}
}
