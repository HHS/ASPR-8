package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import lesson.plugins.model.actors.ContactManager;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.Vaccinator;
import lesson.plugins.model.reports.DiseaseStateReport;
import lesson.plugins.model.reports.VaccineProductionReport;
import lesson.plugins.model.reports.VaccineReport;
import lesson.plugins.model.support.ModelReportLabel;

public final class ModelPlugin {
	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new PopulationLoader()::init);
					c.addActor(new ContactManager()::init);
					c.addActor(new Vaccinator()::init);

					c.addReport(new DiseaseStateReport(ModelReportLabel.DISEASE_STATE_REPORT,
							ReportPeriod.END_OF_SIMULATION)::init);//
					c.addReport(new VaccineReport(ModelReportLabel.VACCINE_REPORT, ReportPeriod.DAILY)::init);//
					c.addReport(new VaccineProductionReport(ModelReportLabel.VACCINE_PRODUCTION_REPORT,
							ReportPeriod.DAILY)::init);//

				}).build();
	}

	private ModelPlugin() {

	}
}
