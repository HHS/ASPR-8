package lessons.lesson_08.plugins.model;

import lessons.lesson_08.plugins.disease.DiseaseDataManager;
import lessons.lesson_08.plugins.policy.PolicyDataManager;
import nucleus.ActorContext;

public final class ModelActor {

	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		System.out.println("r0 = " + diseaseDataManager.getR0());
		System.out.println("asymptomatic days = " + diseaseDataManager.getAsymptomaticDays());
		System.out.println("symptomatic days = " + diseaseDataManager.getSymptomaticDays());
		
		PolicyDataManager policyDataManager = actorContext.getDataManager(PolicyDataManager.class);
		System.out.println("school closing infection rate = "+policyDataManager.getSchoolClosingInfectionRate());
		System.out.println("distribute vaccine locally = "+policyDataManager.distributeVaccineLocally());
	}
}
