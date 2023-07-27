package lesson.plugins.model;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import lesson.plugins.disease.DiseaseDataManager;

public final class ModelActor {

	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		for (int i = 0; i < 3; i++) {
			double deltaTime = randomGenerator.nextDouble() * 10 + 1;
			actorContext.addPlan((c) -> {
				double newR0Value = randomGenerator.nextDouble() + 1;
				diseaseDataManager.setR0(newR0Value);
			}, actorContext.getTime() + deltaTime);
		}
	}
}
