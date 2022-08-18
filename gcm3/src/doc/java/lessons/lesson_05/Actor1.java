
package lessons.lesson_05;

import nucleus.ActorContext;

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
				beta *=2;
				exampleDataManager.setBeta(beta);
			}, planTime);
		}

	}
}



