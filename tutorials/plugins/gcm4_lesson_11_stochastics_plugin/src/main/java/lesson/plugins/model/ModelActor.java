package lesson.plugins.model;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.disease.DiseaseDataManager;
import nucleus.ActorContext;
import plugins.stochastics.datamanagers.StochasticsDataManager;

public final class ModelActor {

	public void init(ActorContext actorContext) {
		DiseaseDataManager diseaseDataManager = actorContext.getDataManager(DiseaseDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		for (int i = 0; i < 3; i++) {
			double deltaTime = randomGenerator.nextDouble()*10+1;
			actorContext.addPlan((c)->{
				double newR0Value = randomGenerator.nextDouble()+1;
				diseaseDataManager.setR0(newR0Value);
			}, actorContext.getTime()+deltaTime);
		}
	}
}
