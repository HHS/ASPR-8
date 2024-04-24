package gov.hhs.aspr.ms.gcm.lessons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePluginData;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ExperimentParameterData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;

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
		return PolicyPluginData.builder()//
				.setDistributeVaccineLocally(true)//
				.setSchoolClosingInfectionRate(0.05)//
				.build();
	}

	private static Dimension getPolicyDimension() {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		List<Double> schoolClosingInfectionRates = new ArrayList<>();
		schoolClosingInfectionRates.add(0.05);
		schoolClosingInfectionRates.add(0.10);
		schoolClosingInfectionRates.add(0.15);
		schoolClosingInfectionRates.add(0.20);

		for (Double schoolClosingInfectionRate : schoolClosingInfectionRates) {
			builder.addLevel((context) -> {
				PolicyPluginData.Builder pluginDataBuilder = context
						.getPluginDataBuilder(PolicyPluginData.Builder.class);
				pluginDataBuilder.setSchoolClosingInfectionRate(schoolClosingInfectionRate);

				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(schoolClosingInfectionRate));

				return result;
			});//
		}

		builder.addMetaDatum("school_closing_infection_rate");//

		return builder.build();

	}

	/* start code_ref=stochastics_plugin_stochastics_dimension|code_cap=The stochastics dimension introduces three random seeds that will be used in creating the scenarios.  Note that seeds are generated outside of the levels within the dimension. */
	private static Dimension getStochasticsDimension(long seed) {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		Random random = new Random(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			seedValues.add(random.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = context
						.getPluginDataBuilder(StochasticsPluginData.Builder.class);
				long seedValue = seedValues.get(i);
				WellState wellState = WellState.builder().setSeed(seedValue).build();
				stochasticsPluginDataBuilder.setMainRNGState(wellState);

				ArrayList<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Long.toString(seedValue) + "L");

				return result;
			});
		});

		builder.addMetaDatum("seed index");//
		builder.addMetaDatum("seed value");//

		return builder.build();
	}

	/* end */

	/* start code_ref=stochastics_plugin_example_11_B|code_cap=The experiment uses the stochastics dimension, resulting in twelve scenarios.*/
	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();
		WellState wellState = WellState.builder().setSeed(0).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState)
				.build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		Dimension policyDimension = getPolicyDimension();
		Dimension stochasticsDimension = getStochasticsDimension(539847398756272L);

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(4)//
				.build();

		Experiment.builder()//
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
	/* end */
}
