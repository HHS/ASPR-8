package lessons.lesson_05;

import nucleus.ActorContext;
import nucleus.EventFilter;

public final class Actor2 {
	public void init(ActorContext actorContext) {

		EventFilter<AlphaChangeEvent> eventFilter = EventFilter.builder(AlphaChangeEvent.class).build();

		actorContext.subscribe(eventFilter, (context, event) -> {
			System.out.println("Actor2 observes event " + event + " at time = " + context.getTime());
		});
	}
}