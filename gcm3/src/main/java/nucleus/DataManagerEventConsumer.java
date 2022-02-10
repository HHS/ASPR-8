package nucleus;

/**
 * 
 * Defines the handling of an event by a resolver.
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public interface DataManagerEventConsumer<T extends Event> {
	public void handleEvent(DataManagerContext context, T event);
}
