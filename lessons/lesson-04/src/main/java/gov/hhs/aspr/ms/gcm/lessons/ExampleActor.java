
package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;

/* start code_ref=data_managers_example_actor|code_cap=The example actor initializes by making plans to update the alpha property on a daily basis.*/
public final class ExampleActor {

	public void init(ActorContext actorContext) {
		System.out.println("Example Actor is initialized and will plan to set Alpha");

		ExampleDataManager exampleDataManager = actorContext.getDataManager(ExampleDataManager.class);

		for (double planTime = 0; planTime < 10; planTime++) {
			actorContext.addPlan((context) -> {
				int alpha = exampleDataManager.getAlpha();
				alpha++;
				exampleDataManager.setAlpha(alpha);
			}, planTime);
		}
	}
}
/* end */
