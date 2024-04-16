package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a person's resource level has changed.
 */
@Immutable
public record PersonResourceUpdateEvent(PersonId personId, ResourceId resourceId, long previousResourceLevel,
		long currentResourceLevel) implements Event {
}
