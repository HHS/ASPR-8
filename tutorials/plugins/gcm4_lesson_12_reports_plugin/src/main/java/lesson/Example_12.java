package lesson;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lesson.plugins.family.FamilyPlugin;
import lesson.plugins.family.FamilyPluginData;
import lesson.plugins.model.ModelLabel;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.person.PersonPlugin;
import lesson.plugins.vaccine.VaccinePlugin;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.reports.support.NIOReportItemHandler;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;

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
		
		WellState wellState = WellState.builder().setSeed(452363456L).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		FamilyPluginData familyPluginData = FamilyPluginData.builder()//
															.setFamilyCount(30)//
															.setMaxFamilySize(5)//
															.build();
		Plugin familyPlugin = FamilyPlugin.getFamilyPlugin(familyPluginData);		
		
		
		Path outputDirectory = Paths.get("C:\\temp\\gcm");		
		
		NIOReportItemHandler nioReportItemHandler = NIOReportItemHandler.builder()//
				.addReport(ModelLabel.FAMILY_VACCINE_REPORT, outputDirectory.resolve(Paths.get("family_vaccine_report.xls")))//
				.addReport(ModelLabel.HOURLY_VACCINE_REPORT, outputDirectory.resolve(Paths.get("hourly_vaccine_report.xls")))//
				.addReport(ModelLabel.STATELESS_VACCINE_REPORT, outputDirectory.resolve(Paths.get("stateless_vaccine_report.xls")))//
				.build();

		
		Dimension familySizeDimension = getFamilySizeDimension();
		
		Experiment	.builder()//
					.addPlugin(vaccinePlugin)//
					.addPlugin(familyPlugin)//
					.addPlugin(personPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(stochasticsPlugin)//
					.addDimension(familySizeDimension)//
					.addExperimentContextConsumer(nioReportItemHandler)//					
					.build()//
					.execute();
	}
}
