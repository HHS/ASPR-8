package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import lesson.plugins.model.actors.PopulationLoader;
import lesson.plugins.model.actors.QuestionnaireDistributor;
import lesson.plugins.model.actors.ResourceLoader;
import lesson.plugins.model.actors.TreatmentManager;
import lesson.plugins.model.reports.DeathReport;
import lesson.plugins.model.reports.QuestionnaireReport;
import lesson.plugins.model.reports.TreatmentReport;

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
