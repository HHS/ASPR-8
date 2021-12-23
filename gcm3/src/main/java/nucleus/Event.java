package nucleus;

/**
 * Represents an action. Events can represent the attempted mutation of a data
 * state by an agent or the observation of a data state change. Typically,
 * agents generate mutation events that resolvers resolve into changes to data
 * views. Resolvers generate observation events in response to those changes
 * which are in turn handled by other resolvers and agents.
 * 
 * @author Shawn Hatch
 *
 */
public interface Event {
	/**
	 * Returns the non-null primary key value for the event. This value is used
	 * to reduce the run-time associated with the distribution of observation
	 * events. The default value is the class of this event.
	 */
	public default Object getPrimaryKeyValue() {
		return getClass();
	}
}
