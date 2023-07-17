
package lesson;

import nucleus.ActorContext;
import nucleus.EventFilter;
/* start code_ref=events_actor_3_reacts_to_BetaChangeEvent*/
public final class Actor3 {
	public void init(ActorContext actorContext) {
		EventFilter<BetaChangeEvent> eventFilter = EventFilter.builder(BetaChangeEvent.class).build();
		actorContext.subscribe(eventFilter, (context, event) -> {
			System.out.println("Actor3 observes event " + event + " at time = " + context.getTime());
		});
	}
}
/* end */