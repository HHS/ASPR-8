package lessons.lesson_08;

import lessons.lesson_08.plugins.disease.DiseasePlugin;
import lessons.lesson_08.plugins.disease.DiseasePluginData;
import lessons.lesson_08.plugins.model.ModelPlugin;
import lessons.lesson_08.plugins.policy.PolicyPlugin;
import lessons.lesson_08.plugins.policy.PolicyPluginData;
import nucleus.Plugin;
import nucleus.Simulation;

 public final class Example_8 {

	private Example_8() {
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

	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();		
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);
		
		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);
		
		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Simulation	.builder()//
					.addPlugin(diseasePlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(policyPlugin)//
					.build()//
					.execute();
	}
 }
 
 
