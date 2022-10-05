package lessons.lesson_13.plugins.model;

import java.util.stream.IntStream;

import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;

public final class DeltaActor {

	public void init(ActorContext actorContext) {

		int count = 10;		
		IntStream.range(0, count).forEach((i) -> {
			actorContext.addPlan((c) -> {
				GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
				Double alpha = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ALPHA);
				Double beta = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.BETA);
				double delta = (beta - alpha) * i / count + alpha;
				globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.DELTA, delta);
			}, i + 1);
		});
	}
}
