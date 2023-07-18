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
import nucleus.ExperimentParameterData;
import nucleus.FunctionalDimension;
import nucleus.Plugin;

public final class Example_10_C {

	private Example_10_C() {
	}

	private static DiseasePluginData getDiseasePluginData() {
		return DiseasePluginData.builder()//
				.setR0(1.5)//
				.setAsymptomaticDays(4.0)//
				.setSymptomaticDays(12.0)//
				.build();
	}

	private static PolicyPluginData getPolicyPluginData() {
		return PolicyPluginData.builder()//
				.setDistributeVaccineLocally(true)//
				.setSchoolClosingInfectionRate(0.05)//
				.build();
	}

	private static Dimension getR0Dimension() {

		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		List<Double> r0Values = new ArrayList<>();
		r0Values.add(1.5);
		r0Values.add(2.0);
		r0Values.add(2.5);

		for (Double r0 : r0Values) {
			builder.addLevel((context) -> {
				DiseasePluginData.Builder pluginDataBuilder = context
						.getPluginDataBuilder(DiseasePluginData.Builder.class);
				pluginDataBuilder.setR0(r0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(r0));
				return result;
			});//
		}
		builder.addMetaDatum("r0");//

		return builder.build();

	}

	private static Dimension getPolicyDimension() {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		List<Double> schoolClosingInfectionRates = new ArrayList<>();
		schoolClosingInfectionRates.add(0.05);
		schoolClosingInfectionRates.add(0.10);

		List<Boolean> localVaccineDistributionValues = new ArrayList<>();
		localVaccineDistributionValues.add(false);
		localVaccineDistributionValues.add(true);

		for (Boolean localVaccineDistribution : localVaccineDistributionValues) {
			for (Double schoolClosingInfectionRate : schoolClosingInfectionRates) {
				builder.addLevel((context) -> {
					PolicyPluginData.Builder pluginDataBuilder = context
							.getPluginDataBuilder(PolicyPluginData.Builder.class);
					pluginDataBuilder.setSchoolClosingInfectionRate(schoolClosingInfectionRate);
					pluginDataBuilder.setDistributeVaccineLocally(localVaccineDistribution);

					ArrayList<String> result = new ArrayList<>();
					result.add(Double.toString(schoolClosingInfectionRate));
					result.add(Boolean.toString(localVaccineDistribution));
					return result;
				});//
			}
		}
		builder.addMetaDatum("school_closing_infection_rate");//
		builder.addMetaDatum("distribute_vaccine_locally");//

		return builder.build();

	}

	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Dimension r0Dimension = getR0Dimension();

		Dimension policyDimension = getPolicyDimension();

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(4)//
				.build();

		Experiment.builder()//
				.addPlugin(diseasePlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(policyPlugin)//
				.addDimension(r0Dimension)//
				.addDimension(policyDimension)//
				.addExperimentContextConsumer(new OutputConsumer_C())//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();
	}
}
