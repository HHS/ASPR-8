package lessons.lesson_12;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lessons.lesson_12.plugins.family.FamilyPlugin;
import lessons.lesson_12.plugins.family.FamilyPluginData;
import lessons.lesson_12.plugins.model.ModelPlugin;
import lessons.lesson_12.plugins.model.ModelReportId;
import lessons.lesson_12.plugins.person.PersonPlugin;
import lessons.lesson_12.plugins.vaccine.FamilyVaccineReport;
import lessons.lesson_12.plugins.vaccine.HourlyVaccineReport;
import lessons.lesson_12.plugins.vaccine.StatelessVaccineReport;
import lessons.lesson_12.plugins.vaccine.VaccinePlugin;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

public final class Example_12 {

	private Example_12() {
	}
	
	private static Dimension getFamilySizeDimension() {
		Dimension.Builder builder = Dimension.builder();//

		List<Integer> maxFamilySizes = new ArrayList<>();
		
		maxFamilySizes.add(3);
		maxFamilySizes.add(5);
		maxFamilySizes.add(7);
		maxFamilySizes.add(10);
		

		for (Integer maxFamilySize : maxFamilySizes) {
			builder.addLevel((context) -> {
				FamilyPluginData.Builder pluginDataBuilder = 
						context.get(FamilyPluginData.Builder.class);
				pluginDataBuilder.setMaxFamilySize(maxFamilySize);

				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(maxFamilySize));

				return result;
			});//
		}

		builder.addMetaDatum("max_family_size");//

		return builder.build();

	}

	
	public static void main(String[] args) {

		Plugin personPlugin = PersonPlugin.getPersonPlugin();

		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();
		
		Plugin modelPlugin = ModelPlugin.getModelPlugin();
		
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(452363456L).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		FamilyPluginData familyPluginData = FamilyPluginData.builder()//
															.setFamilyCount(30)//
															.setMaxFamilySize(5)//
															.build();
		Plugin familyPlugin = FamilyPlugin.getFamilyPlugin(familyPluginData);		
		
		ReportsPluginData reportsPluginData = ReportsPluginData.builder()//
				.addReport(()->new FamilyVaccineReport(ModelReportId.FAMILY_VACCINE_REPORT)::init)//
				.addReport(()->new HourlyVaccineReport(ModelReportId.HOURLY_VACCINE_REPORT, ReportPeriod.HOURLY)::init)//
				.addReport(()->new StatelessVaccineReport(ModelReportId.STATELESS_VACCINE_REPORT, ReportPeriod.HOURLY)::init)//
				.build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		
		Path outputDirectory = Paths.get("C:\\temp\\gcm");		
		
		NIOReportItemHandler nioReportItemHandler = NIOReportItemHandler.builder()//
				.addReport(ModelReportId.FAMILY_VACCINE_REPORT, outputDirectory.resolve(Paths.get("family_vaccine_report.xls")))//
				.addReport(ModelReportId.HOURLY_VACCINE_REPORT, outputDirectory.resolve(Paths.get("hourly_vaccine_report.xls")))//
				.addReport(ModelReportId.STATELESS_VACCINE_REPORT, outputDirectory.resolve(Paths.get("stateless_vaccine_report.xls")))//
				.build();

		
		Dimension familySizeDimension = getFamilySizeDimension();
		
		Experiment	.builder()//
					.addPlugin(vaccinePlugin)//
					.addPlugin(familyPlugin)//
					.addPlugin(personPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(reportPlugin)//
					.addDimension(familySizeDimension)//
					.addExperimentContextConsumer(nioReportItemHandler)//					
					.build()//
					.execute();
	}
}
