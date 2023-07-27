package lesson.plugins.vaccine;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import lesson.plugins.family.FamilyPluginId;
import lesson.plugins.model.ModelLabel;
import lesson.plugins.person.PersonPluginId;
import lesson.plugins.vaccine.datamanagers.VaccinationDataManager;
import lesson.plugins.vaccine.reports.FamilyVaccineReport;
import lesson.plugins.vaccine.reports.HourlyVaccineReport;
import lesson.plugins.vaccine.reports.StatelessVaccineReport;

public class VaccinePlugin {

	private VaccinePlugin() {
	}

	public static Plugin getVaccinePlugin() {

		return Plugin.builder()//
				.setPluginId(VaccinePluginId.PLUGIN_ID)//
				.addPluginDependency(PersonPluginId.PLUGIN_ID)//
				.addPluginDependency(FamilyPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					c.addDataManager(new VaccinationDataManager());
					c.addReport(new FamilyVaccineReport(ModelLabel.FAMILY_VACCINE_REPORT)::init);//
					c.addReport(new HourlyVaccineReport(ModelLabel.HOURLY_VACCINE_REPORT, ReportPeriod.HOURLY)::init);//
					c.addReport(
							new StatelessVaccineReport(ModelLabel.STATELESS_VACCINE_REPORT, ReportPeriod.HOURLY)::init);//
				})//
				.build();

	}
}
