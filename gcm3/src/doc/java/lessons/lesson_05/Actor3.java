
package lessons.lesson_05;

import nucleus.ActorContext;

public final class Actor3 {
	public void init(ActorContext actorContext) {
		actorContext.subscribe(BetaChangeEvent.class, (context, event) -> {
			System.out.println("Actor3 observes event "+ event+" at time = "+context.getTime());			
		});
	}
}
