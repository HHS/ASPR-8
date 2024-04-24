package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupTypeId;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a group type has been created
 */
@Immutable
public record GroupTypeAdditionEvent(GroupTypeId groupTypeId) implements Event {
}
