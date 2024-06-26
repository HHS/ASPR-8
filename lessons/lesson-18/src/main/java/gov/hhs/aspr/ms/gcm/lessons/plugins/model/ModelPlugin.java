package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PopulationLoader;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.QuestionnaireDistributor;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.ResourceLoader;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.TreatmentManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.DeathReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.QuestionnaireReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.TreatmentReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class ModelPlugin {
	private ModelPlugin() {

	}

	public static Plugin getModelPlugin() {
		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID).setInitializer((c) -> {
					c.addActor(new PopulationLoader()::init);
					c.addActor(new ResourceLoader()::init);
					c.addActor(new TreatmentManager()::init);
					c.addActor(new QuestionnaireDistributor()::init);

					c.addReport(new TreatmentReport(ModelReportLabel.TREATMENT_REPORT)::init);//
					c.addReport(new DeathReport(ModelReportLabel.DEATH_REPORT)::init);//
					c.addReport(new QuestionnaireReport(ModelReportLabel.QUESTIONNAIRE_REPORT)::init);//

				}).build();
	}
}
