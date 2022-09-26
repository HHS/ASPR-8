package lessons.lesson_12;

import java.nio.file.Path;
import java.nio.file.Paths;

import lessons.lesson_11.plugins.model.ModelReportId;
import lessons.lesson_12.plugins.family.FamilyPlugin;
import lessons.lesson_12.plugins.family.FamilyPluginData;
import lessons.lesson_12.plugins.model.ModelPlugin;
import lessons.lesson_12.plugins.person.PersonPlugin;
import lessons.lesson_12.plugins.vaccine.FamilyVaccineReport;
import lessons.lesson_12.plugins.vaccine.VaccinePlugin;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

public final class Example_12 {

	private Example_12() {
	}

	/**
	 * Emphasize the lack of order, and the plugin dependencies
	 */
	public static void main(String[] args) {

		Plugin personPlugin = PersonPlugin.getPersonPlugin();

		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();

		FamilyPluginData familyPluginData = FamilyPluginData.builder()//
															.setFamilyCount(30)//
															.setMaxFamilySize(5)//
															.build();
		Plugin familyPlugin = FamilyPlugin.getFamilyPlugin(familyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(452363456L).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		
		ReportsPluginData reportsPluginData = ReportsPluginData.builder()//
				.addReport(()->new FamilyVaccineReport(ModelReportId.FAMILY_VACCINE_REPORT)::init)//
				.build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		
		Path path = Paths.get("C:\\temp\\family_vaccine_report.xls");
		
		
		NIOReportItemHandler nioReportItemHandler = NIOReportItemHandler.builder()//
				.addReport(ModelReportId.FAMILY_VACCINE_REPORT, path)//
				.build();

		Experiment	.builder()//
					.addPlugin(vaccinePlugin)//
					.addPlugin(familyPlugin)//
					.addPlugin(personPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(reportPlugin)//
					.addExperimentContextConsumer(nioReportItemHandler)//					
					.build()//
					.execute();
	}
}
