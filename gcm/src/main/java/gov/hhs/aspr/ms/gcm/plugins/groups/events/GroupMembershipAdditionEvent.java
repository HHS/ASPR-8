package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;

/**
 * Event to indicating that person was added to a group
 */
@Immutable
public record GroupMembershipAdditionEvent(PersonId personId, GroupId groupId) implements Event {
}
