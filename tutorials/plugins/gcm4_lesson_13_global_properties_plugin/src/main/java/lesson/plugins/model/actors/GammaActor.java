package lesson.plugins.model.actors;

import java.util.stream.IntStream;

import lesson.plugins.model.support.GlobalProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;

/* start code_ref= global_proerties_plugin_gamma_actor*/
public final class GammaActor {

	public void init(ActorContext actorContext) {
		int count = 10;
		IntStream.range(0, count).forEach((i) -> {
			actorContext.addPlan((c) -> {
				GlobalPropertiesDataManager globalPropertiesDataManager = c
						.getDataManager(GlobalPropertiesDataManager.class);
				Double alpha = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ALPHA);
				Double beta = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.BETA);
				double delta = (beta - alpha) * i / count + alpha;
				globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.GAMMA, delta);
			}, i + 1);
		});
	}
}
/* end */
