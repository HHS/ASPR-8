package lesson.plugins.model;

import lesson.plugins.disease.DiseaseDataManager;
import lesson.plugins.policy.PolicyDataManager;
import nucleus.ActorContext;

public final class ModelActor {
	/* start code_ref=output_model_actor_init */
	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		actorContext.releaseOutput("Model Actor initializing");
		String tab = "\t";
		actorContext.releaseOutput(tab + "r0 = " + diseaseDataManager.getR0());
		actorContext.releaseOutput(tab + "asymptomatic days = " + diseaseDataManager.getAsymptomaticDays());
		actorContext.releaseOutput(tab + "symptomatic days = " + diseaseDataManager.getSymptomaticDays());
		PolicyDataManager policyDataManager = actorContext.getDataManager(PolicyDataManager.class);
		actorContext.releaseOutput(
				tab + "school closing infection rate = " + policyDataManager.getSchoolClosingInfectionRate());
		actorContext
				.releaseOutput(tab + "distribute vaccine locally = " + policyDataManager.distributeVaccineLocally());
	}
	/* end */
}
