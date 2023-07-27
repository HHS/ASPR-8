package lesson.plugins.vaccine;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.vaccine.reports.VaccineReport;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin.builder()//
				.setPluginId(VaccinePluginId.PLUGIN_ID)//
				.addPluginDependency(PeoplePluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addDataManager(new VaccinationDataManager());
					c.addReport(
							new VaccineReport(ModelReportLabel.VACCINATION, ReportPeriod.END_OF_SIMULATION, 6)::init);
				})//
				.build();

	}
}
