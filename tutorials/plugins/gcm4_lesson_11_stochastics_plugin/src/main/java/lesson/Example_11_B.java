package lesson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import lesson.plugins.disease.DiseasePlugin;
import lesson.plugins.disease.DiseasePluginData;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.policy.PolicyPlugin;
import lesson.plugins.policy.PolicyPluginData;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentParameterData;
import nucleus.Plugin;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;

public final class Example_11_B {

	private Example_11_B() {
	}

	private static DiseasePluginData getDiseasePluginData() {
		return DiseasePluginData.builder()//
								.setR0(1.5)//
								.setAsymptomaticDays(4.0)//
								.setSymptomaticDays(12.0)//
								.build();
	}

	private static PolicyPluginData getPolicyPluginData() {
		return PolicyPluginData	.builder()//
								.setDistributeVaccineLocally(true)//
								.setSchoolClosingInfectionRate(0.05)//
								.build();
	}

	private static Dimension getPolicyDimension() {
		Dimension.Builder builder = Dimension.builder();//

		List<Double> schoolClosingInfectionRates = new ArrayList<>();
		schoolClosingInfectionRates.add(0.05);
		schoolClosingInfectionRates.add(0.10);
		schoolClosingInfectionRates.add(0.15);
		schoolClosingInfectionRates.add(0.20);

		for (Double schoolClosingInfectionRate : schoolClosingInfectionRates) {
			builder.addLevel((context) -> {
				PolicyPluginData.Builder pluginDataBuilder = context.get(PolicyPluginData.Builder.class);
				pluginDataBuilder.setSchoolClosingInfectionRate(schoolClosingInfectionRate);

				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(schoolClosingInfectionRate));

				return result;
			});//
		}

		builder.addMetaDatum("school_closing_infection_rate");//

		return builder.build();

	}

	private static Dimension getStochasticsDimension(long seed) {
		Dimension.Builder builder = Dimension.builder();//

		Random random = new Random(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			seedValues.add(random.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = 
						context.get(StochasticsPluginData.Builder.class);
				long seedValue = seedValues.get(i);
				WellState wellState = WellState.builder().setSeed(seedValue).build();
				stochasticsPluginDataBuilder.setMainRNGState(wellState);

				ArrayList<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Long.toString(seedValue)+"L");

				return result;
			});
		});

		builder.addMetaDatum("seed index");//
		builder.addMetaDatum("seed value");//

		return builder.build();
	}

	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();
		WellState wellState = WellState.builder().setSeed(0).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		Dimension policyDimension = getPolicyDimension();
		Dimension stochasticsDimension = getStochasticsDimension(539847398756272L);

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(4)//				
				.build();
		
		
		Experiment	.builder()//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(diseasePlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(policyPlugin)//
					.addDimension(policyDimension)//
					.addDimension(stochasticsDimension)//
					.addExperimentContextConsumer(new SimpleOutputConsumer())//
					.setExperimentParameterData(experimentParameterData)//
					.build()//
					.execute();
	}
}
