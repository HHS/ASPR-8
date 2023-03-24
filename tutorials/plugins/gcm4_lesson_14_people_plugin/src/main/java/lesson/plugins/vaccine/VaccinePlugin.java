package lesson.plugins.vaccine;

import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;
import lesson.plugins.vaccine.reports.VaccineReport;
import nucleus.Plugin;
import plugins.people.PeoplePluginId;
import plugins.reports.support.ReportPeriod;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin	.builder()//
						.setPluginId(VaccinePluginId.PLUGIN_ID)//
						.addPluginDependency(PeoplePluginId.PLUGIN_ID)//						
						.setInitializer((c) -> {
							c.addDataManager(new VaccinationDataManager());
							c.addReport(new VaccineReport(ModelReportLabel.VACCINATION, ReportPeriod.DAILY, 6)::init);
						})//
						.build();

	}
}
