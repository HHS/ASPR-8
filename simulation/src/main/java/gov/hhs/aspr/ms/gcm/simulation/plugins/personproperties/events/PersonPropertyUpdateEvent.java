package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 */
@Immutable
public record PersonPropertyUpdateEvent(PersonId personId, PersonPropertyId personPropertyId,
		Object previousPropertyValue, Object currentPropertyValue) implements Event {

	@SuppressWarnings("unchecked")
	public <T> T getCurrentPropertyValue() {
		return (T) currentPropertyValue;
	}

	@SuppressWarnings("unchecked")
	public <T> T getPreviousPropertyValue() {
		return (T) previousPropertyValue;
	}

}
