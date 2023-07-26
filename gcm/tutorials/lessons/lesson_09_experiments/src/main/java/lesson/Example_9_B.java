package lesson;

import java.util.ArrayList;

import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import lesson.plugins.disease.DiseasePlugin;
import lesson.plugins.disease.DiseasePluginData;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.policy.PolicyPlugin;
import lesson.plugins.policy.PolicyPluginData;

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

	/* start code_ref=experiements_example_9_ B */
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
