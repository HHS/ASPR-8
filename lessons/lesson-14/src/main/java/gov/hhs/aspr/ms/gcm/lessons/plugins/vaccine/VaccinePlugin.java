package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelReportLabel;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.datamanagers.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports.VaccineReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin.builder()//
				.setPluginId(VaccinePluginId.PLUGIN_ID)//
				.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addDataManager(new VaccinationDataManager());
					c.addReport(new VaccineReport(ModelReportLabel.VACCINATION, ReportPeriod.DAILY, 6)::init);
				})//
				.build();

	}
}
