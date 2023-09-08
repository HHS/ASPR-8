
package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;

/* start code_ref=events_actor_1_mutates_values|code_cap=Actor 1 schedules updates to both the alpha and beta properties.*/
public final class Actor1 {

	public void init(ActorContext actorContext) {
		ExampleDataManager exampleDataManager = actorContext.getDataManager(ExampleDataManager.class);

		for (double planTime = 1; planTime <= 10; planTime++) {
			actorContext.addPlan((context) -> {
				int alpha = exampleDataManager.getAlpha();
				alpha++;
				exampleDataManager.setAlpha(alpha);
			}, planTime);
		}

		for (int i = 1; i <= 5; i++) {
			double planTime = i * 3.5;
			actorContext.addPlan((context) -> {
				double beta = exampleDataManager.getBeta();
				beta *= 2;
				exampleDataManager.setBeta(beta);
			}, planTime);
		}

	}
}
/* end */
