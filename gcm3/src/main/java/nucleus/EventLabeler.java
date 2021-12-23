package nucleus;

/**
 * A generics-based class that is used to filter event observations.
 * 
 * See {@linkplain EventLabel} for details.
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 */
public interface EventLabeler<T extends Event> {

	/**
	 * Returns an event label from a given event. This event label must 1)have
	 * the same labeler id as this labeler 2) have the same primary key as the
	 * event and 3) have the same event class as this labeler.
	 */
	public EventLabel<T> getEventLabel(Context context, T event);

	/**
	 * Returns the event class of T
	 */
	public Class<T> getEventClass();

	/**
	 * Returns the unique id of this labeler
	 */
	public EventLabelerId getId();
}
