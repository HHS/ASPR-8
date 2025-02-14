package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.stream.IntStream;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;

/* start code_ref= global_properties_plugin_gamma_actor|code_cap=The gamma actor sets the value of the GAMMA property over time as a function of the ALPHA and BETA properties.*/
public final class GammaActor {

	public void init(ActorContext actorContext) {
		int count = 10;
		IntStream.range(0, count).forEach((i) -> {
			actorContext.addPlan((c) -> {
				GlobalPropertiesDataManager globalPropertiesDataManager = c
						.getDataManager(GlobalPropertiesDataManager.class);
				Double alpha = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ALPHA);
				Double beta = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.BETA);
				double gamma = (beta - alpha) * i / count + alpha;
				globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.GAMMA, gamma);
			}, i + 1);
		});
	}
}
/* end */
