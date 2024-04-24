package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import net.jcip.annotations.Immutable;

/**
 * Event to signal the imminent removal of a group from the simulation
 */
@Immutable
public record GroupImminentRemovalEvent(GroupId groupId) implements Event {
}
