package nucleus;

/**
 * 
 * Defines the handling of an event by an agent.
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public interface AgentEventConsumer<T extends Event> {

	/**
	 * Instructs an agent to handle the given event
	 */
	public void handleEvent(AgentContext context, T event);
}
