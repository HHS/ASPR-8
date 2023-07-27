package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PopulationManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.Vaccinator;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.PopulationTraceReport;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

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
