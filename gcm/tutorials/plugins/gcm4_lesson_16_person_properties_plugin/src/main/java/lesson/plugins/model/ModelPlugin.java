package lesson.plugins.model;

import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.Vaccinator;
import lesson.plugins.model.actors.VaccineEducator;
import lesson.plugins.model.reports.VaccineReport;
import nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new VaccineEducator()::init);
					c.addActor(new Vaccinator()::init);
					c.addActor(new PopulationLoader()::init);
					c.addReport(new VaccineReport(ModelReportLabel.VACCINATION)::init);
				}).build();
	}
}
