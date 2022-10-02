package lessons.lesson_13;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import lessons.lesson_13.plugins.model.ModelPlugin;
import lessons.lesson_13.plugins.model.ModelReportId;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.actors.GlobalPropertyReport;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public final class Example_13 {

	private Example_13() {
	}

	private static GlobalPropertiesPluginData getGlobalPropertiesPluginData() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(2.0)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
																	.setPropertyValueMutability(false)//
																	.build();
		builder.defineGlobalProperty(GlobalProperty.ALPHA, propertyDefinition);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(5.0)//
												.setTimeTrackingPolicy(TimeTrackingPolicy.DO_NOT_TRACK_TIME)//
												.setPropertyValueMutability(false)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.BETA, propertyDefinition);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(1.0)//
												.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
												.setPropertyValueMutability(true)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.DELTA, propertyDefinition);

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
				builder.setGlobalPropertyValue(GlobalProperty.ALPHA, pair.getFirst());
				builder.setGlobalPropertyValue(GlobalProperty.BETA, pair.getSecond());
				return result;
			});
		}

		dimensionBuilder.addMetaDatum(GlobalProperty.ALPHA.toString());
		dimensionBuilder.addMetaDatum(GlobalProperty.BETA.toString());

		return dimensionBuilder.build();
	}

	public static void main(String[] args) {

		GlobalPropertiesPluginData globalPropertiesPluginData = getGlobalPropertiesPluginData();
		Plugin globalPropertiesPlugin = GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		ReportsPluginData reportsPluginData = ReportsPluginData	.builder()//
																.addReport(() -> {
																	return new GlobalPropertyReport(ModelReportId.GLOBAL_PROPERTY_REPORT)::init;
																})//
																.build();

		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);

		NIOReportItemHandler nioReportItemHandler = NIOReportItemHandler.builder()//
																		.addReport(ModelReportId.GLOBAL_PROPERTY_REPORT, Paths.get("C:\\temp\\gcm\\global property report.xls"))//
																		.build();

		Dimension alphaBetaDimension = getAlphaBetaDimension();

		Experiment	.builder()//
					.addPlugin(globalPropertiesPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(reportPlugin)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(alphaBetaDimension)//					
					.build()//
					.execute();//

	}
}
