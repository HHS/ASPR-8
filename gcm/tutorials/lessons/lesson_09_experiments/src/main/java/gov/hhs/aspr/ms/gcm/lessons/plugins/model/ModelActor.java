package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseaseDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyDataManager;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;

public final class ModelActor {

	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		System.out.println("Model Actor initializing");
		String tab = "\t";
		System.out.println(tab + "r0 = " + diseaseDataManager.getR0());
		System.out.println(tab + "asymptomatic days = " + diseaseDataManager.getAsymptomaticDays());
		System.out.println(tab + "symptomatic days = " + diseaseDataManager.getSymptomaticDays());

		PolicyDataManager policyDataManager = actorContext.getDataManager(PolicyDataManager.class);
		System.out
				.println(tab + "school closing infection rate = " + policyDataManager.getSchoolClosingInfectionRate());
		System.out.println(tab + "distribute vaccine locally = " + policyDataManager.distributeVaccineLocally());
		System.out.println();
	}
}
