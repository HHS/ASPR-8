package lesson;

import java.util.ArrayList;
import java.util.List;

import lesson.plugins.disease.DiseasePlugin;
import lesson.plugins.disease.DiseasePluginData;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.policy.PolicyPlugin;
import lesson.plugins.policy.PolicyPluginData;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;

public final class Example_11_A {

	private Example_11_A() {
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

	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(0).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		Dimension policyDimension = getPolicyDimension();

		Experiment	.builder()//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(diseasePlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(policyPlugin)//
					.addDimension(policyDimension)//
					.addExperimentContextConsumer(new SimpleOutputConsumer())//
					.setThreadCount(4)//
					.build()//
					.execute();
	}
	
}
