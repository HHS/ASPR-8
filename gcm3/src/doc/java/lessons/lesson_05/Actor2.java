
package lessons.lesson_05;

import nucleus.ActorContext;

public final class Actor2 {
	public void init(ActorContext actorContext) {
		actorContext.subscribe(AlphaChangeEvent.class, (context, event) -> {
			System.out.println("Actor2 observes event "+ event+" at time = "+context.getTime());			
		});
	}
}
