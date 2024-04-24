package gov.hhs.aspr.ms.gcm.lessons.plugins.model;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseaseDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;

public final class ModelActor {
	/* start code_ref=output_model_actor_init|code_cap=The model actor now reports output via the release output method provided by its context. */
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
