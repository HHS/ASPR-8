package manual.pubsub.globalcomponents;

import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;

public class GammaAgent extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		environment.addPlan(new IncrementPlan(), environment.getTime() + 1);
	}

	@Override
	protected void executePlan(Environment environment, Plan plan) {
		int x = environment.getGlobalPropertyValue(GlobalProperty.X);
		environment.setGlobalPropertyValue(GlobalProperty.X, x + 1);
		int max = environment.getGlobalPropertyValue(GlobalProperty.MAX);
		if (x < max) {
			environment.addPlan(plan, environment.getTime() + 1);
		}
	}

	private static class IncrementPlan implements Plan {

	}

}
