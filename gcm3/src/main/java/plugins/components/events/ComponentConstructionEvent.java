package plugins.components.events;

import java.util.function.Consumer;

import net.jcip.annotations.Immutable;
import nucleus.AgentContext;
import nucleus.Event;
import plugins.components.support.ComponentId;

/**
 * An event for adding component agent to the simulation. The component id will
 * be associated with the agent's id and the AgentContext Consumer will be used
 * as the agent's initial behavior
 * 
 */

@Immutable
public final class ComponentConstructionEvent implements Event {

	private final ComponentId componentId;

	private final Consumer<AgentContext> consumer;

	/**
	 * Construct the ComponentConstructionEvent
	 * 
	 */
	public ComponentConstructionEvent(ComponentId componentId, Consumer<AgentContext> consumer) {
		super();
		this.componentId = componentId;
		this.consumer = consumer;
	}

	/**
	 * Returns the component id used to generate this event
	 */
	public ComponentId getComponentId() {
		return componentId;
	}
	
	/**
	 * Returns the agent context consumer used to generate this event	 */

	public Consumer<AgentContext> getConsumer() {
		return consumer;
	}

}
