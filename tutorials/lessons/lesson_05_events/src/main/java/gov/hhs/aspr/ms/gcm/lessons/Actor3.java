
package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;

/* start code_ref=events_actor_3_reacts_to_BetaChangeEvent|code_cap=Actor 3 reacts to changes in the beta property.*/
public final class Actor3 {
	public void init(ActorContext actorContext) {
		EventFilter<BetaChangeEvent> eventFilter = EventFilter.builder(BetaChangeEvent.class).build();
		actorContext.subscribe(eventFilter, (context, event) -> {
			System.out.println("Actor3 observes event " + event + " at time = " + context.getTime());
		});
	}
}
/* end */