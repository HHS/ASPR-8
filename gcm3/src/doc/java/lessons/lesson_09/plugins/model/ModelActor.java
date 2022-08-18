package lessons.lesson_09.plugins.model;

import lessons.lesson_09.plugins.disease.DiseaseDataManager;
import lessons.lesson_09.plugins.policy.PolicyDataManager;
import nucleus.ActorContext;

public final class ModelActor {

	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		System.out.println("Model Actor initializing");
		String tab = "\t";
		System.out.println(tab+"r0 = " + diseaseDataManager.getR0());
		System.out.println(tab+"asymptomatic days = " + diseaseDataManager.getAsymptomaticDays());
		System.out.println(tab+"symptomatic days = " + diseaseDataManager.getSymptomaticDays());
		
		PolicyDataManager policyDataManager = actorContext.getDataManager(PolicyDataManager.class);
		System.out.println(tab+"school closing infection rate = "+policyDataManager.getSchoolClosingInfectionRate());
		System.out.println(tab+"distribute vaccine locally = "+policyDataManager.distributeVaccineLocally());
		System.out.println();
	}
}
