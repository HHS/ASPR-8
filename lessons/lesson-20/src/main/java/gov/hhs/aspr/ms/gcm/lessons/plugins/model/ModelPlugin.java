package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.ContactManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.PopulationLoader;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors.VaccinatorManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports.PersonStatusReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.ModelReportLabel;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

public final class ModelPlugin {
	
	public static Plugin getModelPlugin() {

		return Plugin.builder()//
				.setPluginId(ModelPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addActor(new PopulationLoader()::init);
					c.addActor(new ContactManager()::init);
					c.addActor(new VaccinatorManager()::init);
					c.addReport(new PersonStatusReport(ModelReportLabel.DISEASE_STATE_REPORT)::init);
				}).build();
	}

	private ModelPlugin() {
	}
}
