package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.ReportsPluginId;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.Vaccinator;
import lesson.plugins.model.actors.VaccineEducator;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.addPluginDependency(ReportsPluginId.PLUGIN_ID)//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new VaccineEducator()::init);
					c.addActor(new Vaccinator()::init);
					c.addActor(new PopulationLoader()::init);
				}).build();
	}
}
