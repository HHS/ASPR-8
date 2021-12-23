package plugins.globals.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.globals.support.GlobalPropertyId;

/**
 * An event for setting the value of the a global property.
 *
 */
@Immutable
public final class GlobalPropertyValueAssignmentEvent implements Event {

	private final GlobalPropertyId globalPropertyId;

	private final Object globalPropertyValue;

	/**
	 * Constructs the event from the given global property id and value
	 */
	public GlobalPropertyValueAssignmentEvent(GlobalPropertyId globalPropertyId, Object globalPropertyValue) {
		super();
		this.globalPropertyId = globalPropertyId;
		this.globalPropertyValue = globalPropertyValue;
	}

	/**
	 * Returns the global property id for this event
	 */
	public GlobalPropertyId getGlobalPropertyId() {
		return globalPropertyId;
	}

	/**
	 * Returns the global property value for this event
	 */
	public Object getGlobalPropertyValue() {
		return globalPropertyValue;
	}

}
