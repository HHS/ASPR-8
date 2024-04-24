package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PopulationLoader;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.Vaccinator;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.VaccineEducator;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.VaccineReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

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
