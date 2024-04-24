package gov.hhs.aspr.ms.gcm.lessons;

import java.util.ArrayList;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePluginData;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class Example_9_B {

	private Example_9_B() {
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

	
	/* start code_ref=experiments_example_9_B|code_cap=Example 9 B introduces a single dimension that sets the R0 value of the disease plugin data to two values.*/
	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Dimension dimension = FunctionalDimension.builder()//
				.addLevel((context) -> {
					DiseasePluginData.Builder builder = context.getPluginDataBuilder(DiseasePluginData.Builder.class);
					double r0 = 2.5;
					builder.setR0(r0);
					ArrayList<String> result = new ArrayList<>();
					result.add(Double.toString(r0));
					return result;
				})//

				.addLevel((context) -> {
					DiseasePluginData.Builder builder = context.getPluginDataBuilder(DiseasePluginData.Builder.class);
					double r0 = 2.0;
					builder.setR0(r0);
					ArrayList<String> result = new ArrayList<>();
					result.add(Double.toString(r0));
					return result;
				})//

				.addMetaDatum("r0")//

				.build();

		Experiment.builder()//
				.addPlugin(diseasePlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(policyPlugin)//
				.addDimension(dimension)//
				.build()//
				.execute();
	}
	/* end */

}
