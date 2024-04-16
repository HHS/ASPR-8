package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * Event to indicating that person was added to a group
 */
@Immutable
public record GroupMembershipAdditionEvent(PersonId personId, GroupId groupId) implements Event {
}
