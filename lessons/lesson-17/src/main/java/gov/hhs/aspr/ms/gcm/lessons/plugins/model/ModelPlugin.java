package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.InfectionManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PopulationLoader;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.SchoolManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.TeleworkManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.ContagionReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.DiseaseStateReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new PopulationLoader()::init);
					c.addActor(new InfectionManager()::init);
					c.addActor(new TeleworkManager()::init);
					c.addActor(new SchoolManager()::init);
					c.addReport(new DiseaseStateReport(ModelReportLabel.DISEASE_STATE,
							ReportPeriod.END_OF_SIMULATION)::init);//
					c.addReport(new ContagionReport(ModelReportLabel.CONTAGION)::init);//
				}).build();
	}
}
