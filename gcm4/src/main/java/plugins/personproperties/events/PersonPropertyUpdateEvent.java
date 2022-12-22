package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 *
 * @author Shawn Hatch
 */

@Immutable
public record PersonPropertyUpdateEvent(PersonId personId,
										PersonPropertyId personPropertyId,
										Object previousPropertyValue,
										Object currentPropertyValue) implements Event {

	/**
	 * Returns this event in the form:
	 * <p>
	 * "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId="
	 * + personPropertyId + ", previousPropertyValue=" + previousPropertyValue +
	 * ", currentPropertyValue=" + currentPropertyValue + "]";
	 */
	@Override
	public String toString() {
		return "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId=" + personPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}


}
