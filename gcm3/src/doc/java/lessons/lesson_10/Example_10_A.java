package lessons.lesson_10;

import lessons.lesson_10.plugins.disease.DiseasePlugin;
import lessons.lesson_10.plugins.disease.DiseasePluginData;
import lessons.lesson_10.plugins.model.ModelPlugin;
import lessons.lesson_10.plugins.policy.PolicyPlugin;
import lessons.lesson_10.plugins.policy.PolicyPluginData;
import nucleus.Plugin;
import nucleus.Simulation;

 public final class Example_10_A {

	private Example_10_A() {
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
		
		Simulation	.builder()//
					.addPlugin(diseasePlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(policyPlugin)//	
					.setOutputConsumer(new OutputConsumer_A())
					.build()//
					.execute();
	}
 }
 
 
 
 
 
