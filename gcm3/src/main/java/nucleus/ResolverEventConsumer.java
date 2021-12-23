package nucleus;

/**
 * 
 * Defines the handling of an event by a resolver.
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public interface ResolverEventConsumer<T extends Event> {

	public void handleEvent(ResolverContext context, T event);
}
