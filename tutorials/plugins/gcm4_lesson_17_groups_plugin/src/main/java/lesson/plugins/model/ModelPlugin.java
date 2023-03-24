package lesson.plugins.model;

import lesson.plugins.model.actors.InfectionManager;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.SchoolManager;
import lesson.plugins.model.actors.TeleworkManager;
import lesson.plugins.model.reports.ContagionReport;
import lesson.plugins.model.reports.DiseaseStateReport;
import nucleus.Plugin;
import plugins.reports.support.ReportPeriod;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin	.builder()//						
						.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {							
							c.addActor(new PopulationLoader()::init);
							c.addActor(new InfectionManager()::init);
							c.addActor(new TeleworkManager()::init);
							c.addActor(new SchoolManager()::init);		
							c.addReport(new DiseaseStateReport(ModelReportLabel.DISEASE_STATE, ReportPeriod.END_OF_SIMULATION)::init);//
							c.addReport(new ContagionReport(ModelReportLabel.CONTAGION)::init);//							
						}).build();
	}
}
