package plugins.globals.events.mutation;

import java.util.function.Consumer;

import net.jcip.annotations.Immutable;
import nucleus.AgentContext;
import nucleus.Event;
import plugins.globals.support.GlobalComponentId;

/**
 * An event for the creation of a global component that occurs after simulation
 * initialization
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public final class GlobalComponentConstructionEvent implements Event {

	private final GlobalComponentId globalComponentId;

	private final Consumer<AgentContext> consumer;

	/**
	 * Constructs this event
	 * 
	 */
	public GlobalComponentConstructionEvent(GlobalComponentId globalComponentId, Consumer<AgentContext> consumer) {
		super();
		this.globalComponentId = globalComponentId;
		this.consumer = consumer;
	}

	/**
	 * Returns the global component id used to create this event
	 */

	public GlobalComponentId getGlobalComponentId() {
		return globalComponentId;
	}

	/**
	 * Returns the consumer used to create this event
	 */
	public Consumer<AgentContext> getConsumer() {
		return consumer;
	}

}
