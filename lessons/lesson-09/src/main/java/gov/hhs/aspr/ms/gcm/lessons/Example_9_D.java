package gov.hhs.aspr.ms.gcm.lessons;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePluginData;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimensionData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class Example_9_D {

	private Example_9_D() {
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

		FunctionalDimensionData.Builder builder = FunctionalDimensionData.builder();//

		List<Double> r0Values = new ArrayList<>();
		r0Values.add(1.5);
		r0Values.add(2.0);
		r0Values.add(2.5);

		for (int i = 0; i < r0Values.size(); i++) {
			Double r0 = r0Values.get(i);
			builder.addValue("Level_" + i, (context) -> {
				DiseasePluginData.Builder pluginDataBuilder = context
						.getPluginDataBuilder(DiseasePluginData.Builder.class);
				pluginDataBuilder.setR0(r0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(r0));
				return result;
			});//
		}	

		builder.addMetaDatum("r0");//

		FunctionalDimensionData dimensionData =  builder.build();
		return new FunctionalDimension(dimensionData);

	}

	/* start code_ref=experiements_policy_dimension|code_cap=A dimension representing school related policies is added.  Note that this dimension has four levels and covers two policies.*/
	private static Dimension getPolicyDimension() {
		FunctionalDimensionData.Builder builder = FunctionalDimensionData.builder();//

		List<Double> schoolClosingInfectionRates = new ArrayList<>();
		schoolClosingInfectionRates.add(0.05);
		schoolClosingInfectionRates.add(0.10);

		List<Boolean> localVaccineDistributionValues = new ArrayList<>();
		localVaccineDistributionValues.add(false);
		localVaccineDistributionValues.add(true);

		for (int i = 0; i < localVaccineDistributionValues.size(); i++) {
			Boolean localVaccineDistribution = localVaccineDistributionValues.get(i);
			for (int j = 0; j < schoolClosingInfectionRates.size(); j++) {
				Double schoolClosingInfectionRate = schoolClosingInfectionRates.get(j);
				builder.addValue("Level_" + i + j, (context) -> {
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

		FunctionalDimensionData dimensionData =  builder.build();
		return new FunctionalDimension(dimensionData);
	}
	/* end */

	/* start code_ref=experiements_example_9_D|code_cap=The new policy dimension is added to the experiment with four levels. The R0 dimension was reduced to three levels.  Thus the experiment will run twelve scenarios.*/
	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Dimension r0Dimension = getR0Dimension();

		Dimension policyDimension = getPolicyDimension();

		Experiment.builder()//
				.addPlugin(diseasePlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(policyPlugin)//
				.addDimension(r0Dimension)//
				.addDimension(policyDimension)//
				.build()//
				.execute();
	}
	/* end */

}
