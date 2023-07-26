package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import lesson.plugins.family.FamilyPlugin;
import lesson.plugins.family.FamilyPluginData;
import lesson.plugins.model.ModelLabel;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.person.PersonPlugin;
import lesson.plugins.vaccine.VaccinePlugin;

public final class Example_12 {

	private Example_12() {
	}

	/* start code_ref=reports_plugin_family_dimension */
	private static Dimension getFamilySizeDimension() {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		List<Integer> maxFamilySizes = new ArrayList<>();

		maxFamilySizes.add(3);
		maxFamilySizes.add(5);
		maxFamilySizes.add(7);
		maxFamilySizes.add(10);

		for (Integer maxFamilySize : maxFamilySizes) {
			builder.addLevel((context) -> {
				FamilyPluginData.Builder pluginDataBuilder = context
						.getPluginDataBuilder(FamilyPluginData.Builder.class);
				pluginDataBuilder.setMaxFamilySize(maxFamilySize);

				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(maxFamilySize));

				return result;
			});//
		}

		builder.addMetaDatum("max_family_size");//

		return builder.build();

	}
	/* end */

	/* start code_ref=reports_plugin_example_12_plugins */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("One output directory argument is required");
		}
		Path outputDirectory = Paths.get(args[0]);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		} else {
			if (!Files.isDirectory(outputDirectory)) {
				throw new IOException("Provided path is not a directory");
			}
		}

		Plugin personPlugin = PersonPlugin.getPersonPlugin();

		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		WellState wellState = WellState.builder().setSeed(452363456L).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState)
				.build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		FamilyPluginData familyPluginData = FamilyPluginData.builder()//
				.setFamilyCount(30)//
				.setMaxFamilySize(5)//
				.build();
		Plugin familyPlugin = FamilyPlugin.getFamilyPlugin(familyPluginData);

		/* end */

		/* start code_ref=reports_plugin_nio */
		NIOReportItemHandler nioReportItemHandler = NIOReportItemHandler.builder()//
				.addReport(ModelLabel.FAMILY_VACCINE_REPORT, outputDirectory.resolve("family_vaccine_report.xls"))//
				.addReport(ModelLabel.HOURLY_VACCINE_REPORT, outputDirectory.resolve("hourly_vaccine_report.xls"))//
				.addReport(ModelLabel.STATELESS_VACCINE_REPORT, outputDirectory.resolve("stateless_vaccine_report.xls"))//
				.addExperimentReport(outputDirectory.resolve("experiment_report.xls")).build();
		/* end */

		/* start code_ref=reports_plugin_example_12_execution */
		Dimension familySizeDimension = getFamilySizeDimension();

		Experiment.builder()//
				.addPlugin(vaccinePlugin)//
				.addPlugin(familyPlugin)//
				.addPlugin(personPlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(stochasticsPlugin)//
				.addDimension(familySizeDimension)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.build()//
				.execute();
		/* end */
	}
}
