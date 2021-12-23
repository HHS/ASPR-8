package manual.pubsub.globalcomponents;

import manual.pubsub.PubsubTest;
import nucleus.AgentContext;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;

public class DeltaAgent extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		environment.observeGlobalPropertyChange(true, GlobalProperty.X);
	}

	@Override
	protected void handleGlobalPropertyChangeObservationEvent(AgentContext context, GlobalPropertyChangeObservationEvent event) {
		PubsubTest.COUNTER++;
	}

}
