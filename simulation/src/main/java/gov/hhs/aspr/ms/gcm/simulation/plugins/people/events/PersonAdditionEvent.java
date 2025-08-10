package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * An event to notify the plugins that a person has been fully added to the
 * simulation.
 */
@Immutable
public record PersonAdditionEvent(PersonId personId) implements Event {
}
