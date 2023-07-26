
package lesson;

import nucleus.ActorContext;

/* start code_ref=data_managers_example_actor*/
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