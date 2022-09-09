package lessons.lesson_09;

import java.util.ArrayList;

import lessons.lesson_09.plugins.disease.DiseasePlugin;
import lessons.lesson_09.plugins.disease.DiseasePluginData;
import lessons.lesson_09.plugins.model.ModelPlugin;
import lessons.lesson_09.plugins.policy.PolicyPlugin;
import lessons.lesson_09.plugins.policy.PolicyPluginData;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;

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
		return PolicyPluginData	.builder()//
								.setDistributeVaccineLocally(true)//
								.setSchoolClosingInfectionRate(0.05)//
								.build();
	}

	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Dimension dimension = Dimension	.builder()//
										.addLevel((context) -> {
											DiseasePluginData.Builder builder = 
													context.get(DiseasePluginData.Builder.class);
											double r0 = 2.5;
											builder.setR0(r0);
											ArrayList<String> result = new ArrayList<>();
											result.add(Double.toString(r0));
											return result;
										})//
										
										.addLevel((context) -> {
											DiseasePluginData.Builder builder = 
													context.get(DiseasePluginData.Builder.class);
											double r0 = 2.0;
											builder.setR0(r0);
											ArrayList<String> result = new ArrayList<>();
											result.add(Double.toString(r0));
											return result;
										})//
										
										
										.addMetaDatum("r0")//

										.build();

		Experiment	.builder()//
					.addPlugin(diseasePlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(policyPlugin)//
					.addDimension(dimension)//
					.build()//
					.execute();
	}
	
	
}
