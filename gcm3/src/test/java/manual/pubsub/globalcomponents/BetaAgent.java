package manual.pubsub.globalcomponents;

import nucleus.AgentContext;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;

public class BetaAgent extends AbstractComponent {

	private Environment environment;

	@Override
	public void init(Environment environment) {
		this.environment = environment;
		environment.observeGlobalPropertyChange(true, GlobalProperty.Y);
	}

	@Override
	protected void executePlan(Environment environment, Plan plan) {
		if (plan instanceof IncrementPlan) {
			IncrementPlan incrementPlan = (IncrementPlan) plan;
			// System.out.println(environment.getCurrentComponentId()+" Setting
			// "+GlobalProperty.X+" = "+ incrementPlan.getNextValue());
			environment.setGlobalPropertyValue(GlobalProperty.X, incrementPlan.getNextValue());
		}
	}

	private static class IncrementPlan implements Plan {
		private final int nextValue;

		public IncrementPlan(int nextValue) {
			this.nextValue = nextValue;
		}

		public int getNextValue() {
			return nextValue;
		}
	}

	@Override
	protected void handleGlobalPropertyChangeObservationEvent(AgentContext context, GlobalPropertyChangeObservationEvent event) {
		if (event.getGlobalPropertyId().equals(GlobalProperty.Y)) {
			Integer x = environment.getGlobalPropertyValue(GlobalProperty.X);
			int max = environment.getGlobalPropertyValue(GlobalProperty.MAX);
			if (x < max) {
				environment.addPlan(new IncrementPlan(x + 1), environment.getTime() + 1);
			}
		}
	}

}
