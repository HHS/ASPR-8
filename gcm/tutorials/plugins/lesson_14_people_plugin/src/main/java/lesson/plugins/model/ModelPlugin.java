package lesson.plugins.model;

import lesson.plugins.model.actors.PopulationManager;
import lesson.plugins.model.actors.Vaccinator;
import lesson.plugins.model.reports.PopulationTraceReport;
import nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new Vaccinator()::init);
					c.addActor(new PopulationManager()::init);
					c.addReport(new PopulationTraceReport(ModelReportLabel.POPULATION_TRACE)::init);
				}).build();
	}
}
