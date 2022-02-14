package nucleus;

/**
 * 
 * Represents an observable data change.
 * 
 * @author Shawn Hatch
 *
 */
public interface Event {
	/**
	 * Returns the non-null primary key value for the event. This value is used
	 * to reduce the run-time associated with the distribution of events. The
	 * default value is the class of this event.
	 */
	public default Object getPrimaryKeyValue() {
		return getClass();
	}
}
