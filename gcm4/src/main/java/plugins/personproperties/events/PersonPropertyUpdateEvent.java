package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 *
 */

@Immutable
public record PersonPropertyUpdateEvent(PersonId personId,
										PersonPropertyId personPropertyId,
										Object previousPropertyValue,
										Object currentPropertyValue) implements Event {
	
	@SuppressWarnings("unchecked")
	public <T> T getCurrentPropertyValue() {
		return (T)currentPropertyValue;	
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getPreviousPropertyValue() {
		return (T)previousPropertyValue;
	}
	
}
