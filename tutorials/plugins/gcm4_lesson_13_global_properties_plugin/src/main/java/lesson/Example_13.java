package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.support.GlobalProperty;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.util.properties.PropertyDefinition;

public final class Example_13 {

	private Example_13() {
	}

	private static GlobalPropertiesPluginData getGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(2.0)//																	
																	.setPropertyValueMutability(false)//
																	.build();
		builder.defineGlobalProperty(GlobalProperty.ALPHA, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(5.0)//												
												.setPropertyValueMutability(false)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.BETA, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(1.0)//												
												.setPropertyValueMutability(true)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.GAMMA, propertyDefinition,0);

		return builder.build();
	}

	private static Dimension getAlphaBetaDimension() {
		List<Pair<Double, Double>> alphaBetaPairs = new ArrayList<>();
		alphaBetaPairs.add(new Pair<>(3.0, 10.0));
		alphaBetaPairs.add(new Pair<>(12.0, 25.0));
		alphaBetaPairs.add(new Pair<>(30.0, 40.0));
		alphaBetaPairs.add(new Pair<>(45.0, 70.0));
		alphaBetaPairs.add(new Pair<>(80.0, 100.0));

		Dimension.Builder dimensionBuilder = Dimension.builder();

		for (Pair<Double, Double> pair : alphaBetaPairs) {
			dimensionBuilder.addLevel((c) -> {
				List<String> result = new ArrayList<>();
				Builder builder = c.get(GlobalPropertiesPluginData.Builder.class);
				builder.setGlobalPropertyValue(GlobalProperty.ALPHA, pair.getFirst(),0);
				builder.setGlobalPropertyValue(GlobalProperty.BETA, pair.getSecond(),0);
				result.add(pair.getFirst().toString());
				result.add(pair.getSecond().toString());
				return result;
			});
		}

		dimensionBuilder.addMetaDatum(GlobalProperty.ALPHA.toString());
		dimensionBuilder.addMetaDatum(GlobalProperty.BETA.toString());

		return dimensionBuilder.build();
	}

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

		GlobalPropertiesPluginData globalPropertiesPluginData = getGlobalPropertiesPluginData();
		
		
		GlobalPropertyReportPluginData globalPropertyReportPluginData = GlobalPropertyReportPluginData	.builder()//
				.setReportLabel(ModelReportLabel.GLOBAL_PROPERTY_REPORT)//				
				.setDefaultInclusion(true)//
				.build();
		
		Plugin globalPropertiesPlugin = GlobalPropertiesPlugin. builder()
				.setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.setGlobalPropertyReportPluginData(globalPropertyReportPluginData)
				.getGlobalPropertiesPlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		

		

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(ModelReportLabel.GLOBAL_PROPERTY_REPORT, //
											outputDirectory.resolve("global property report.xls"))//
									.build();

		Dimension alphaBetaDimension = getAlphaBetaDimension();

		Experiment	.builder()//
					.addPlugin(globalPropertiesPlugin)//
					.addPlugin(modelPlugin)//					
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(alphaBetaDimension)//
					.build()//
					.execute();//

	}
	
	
}
