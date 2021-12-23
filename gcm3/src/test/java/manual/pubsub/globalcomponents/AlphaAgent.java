package manual.pubsub.globalcomponents;

import nucleus.AgentContext;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;

public class AlphaAgent extends AbstractComponent {

	private Environment environment;

	@Override
	public void init(Environment environment) {
		this.environment = environment;
		environment.observeGlobalPropertyChange(true, GlobalProperty.X);
		Integer y = environment.getGlobalPropertyValue(GlobalProperty.Y);
		environment.addPlan(new IncrementPlan(y++), environment.getTime() + 1);
	}

	@Override
	protected void executePlan(Environment environment, Plan plan) {
		if (plan instanceof IncrementPlan) {
			IncrementPlan incrementPlan = (IncrementPlan) plan;
			// System.out.println(environment.getCurrentComponentId()+" Setting
			// "+GlobalProperty.Y+" = "+ incrementPlan.getNextValue());
			environment.setGlobalPropertyValue(GlobalProperty.Y, incrementPlan.getNextValue());
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

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("IncrementPlan [nextValue=");
			builder.append(nextValue);
			builder.append("]");
			return builder.toString();
		}
	}

	@Override
	protected void handleGlobalPropertyChangeObservationEvent(AgentContext context, GlobalPropertyChangeObservationEvent event) {
		if (event.getGlobalPropertyId().equals(GlobalProperty.X)) {
			Integer y = environment.getGlobalPropertyValue(GlobalProperty.Y);
			int max = environment.getGlobalPropertyValue(GlobalProperty.MAX);
			if (y < max) {
				environment.addPlan(new IncrementPlan(y + 1), environment.getTime() + 1);
			}
		}
	}

}
