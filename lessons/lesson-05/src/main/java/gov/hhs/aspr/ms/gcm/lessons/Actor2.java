package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.EventFilter;

/* start code_ref=events_actor_2_reacts_to_AlphaChangeEvent|code_cap=Actor 2 reacts to changes in the alpha property.*/
public final class Actor2 {
	public void init(ActorContext actorContext) {

		EventFilter<AlphaChangeEvent> eventFilter = EventFilter.builder(AlphaChangeEvent.class).build();

		actorContext.subscribe(eventFilter, (context, event) -> {
			System.out.println("Actor2 observes event " + event + " at time = " + context.getTime());
		});
	}
}
/* end */