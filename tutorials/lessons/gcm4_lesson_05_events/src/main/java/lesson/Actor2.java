package lesson;

import nucleus.ActorContext;
import nucleus.EventFilter;
/* start code_ref=events_actor_2_reacts_to_AlphaChangeEvent*/
public final class Actor2 {
	public void init(ActorContext actorContext) {

		EventFilter<AlphaChangeEvent> eventFilter = EventFilter.builder(AlphaChangeEvent.class).build();

		actorContext.subscribe(eventFilter, (context, event) -> {
			System.out.println("Actor2 observes event " + event + " at time = " + context.getTime());
		});
	}
}
/* end */