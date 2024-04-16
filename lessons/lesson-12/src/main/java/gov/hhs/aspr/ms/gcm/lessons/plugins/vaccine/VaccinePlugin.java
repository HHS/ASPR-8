package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyPluginId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelLabel;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.PersonPluginId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.datamanagers.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports.FamilyVaccineReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports.HourlyVaccineReport;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.reports.StatelessVaccineReport;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;

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
