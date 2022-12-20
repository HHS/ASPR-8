package plugins.globalproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.globalproperties.support.GlobalPropertyId;

/**
 * An event released by the global data manager whenever a global property is
 * changed.
 *
 * @author Shawn Hatch
 */

@Immutable
public record GlobalPropertyUpdateEvent(GlobalPropertyId globalPropertyId,
										Object previousPropertyValue,
										Object currentPropertyValue) implements Event {
	/**
	 * Constructs the event.
	 */
	public GlobalPropertyUpdateEvent {
	}

	/**
	 * Returns the global property id
	 */
	public GlobalPropertyId getGlobalPropertyId() {
		return globalPropertyId;
	}

	/**
	 * Returns the previous property value
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns the current property value
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Standard string implementation of the form
	 * <p>
	 * GlobalPropertyUpdateEvent [globalPropertyId=" + globalPropertyId + ",
	 * previousPropertyValue=" + previousPropertyValue + ",
	 * currentPropertyValue=" + currentPropertyValue + "]
	 */
	@Override
	public String toString() {
		return "GlobalPropertyUpdateEvent [globalPropertyId=" + globalPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

}
