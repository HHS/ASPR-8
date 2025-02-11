package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * Indicates that the given person has been removed from the simulation. All
 * references to the person are invalid.
 */
@Immutable
public record PersonRemovalEvent(PersonId personId) implements Event {
}
