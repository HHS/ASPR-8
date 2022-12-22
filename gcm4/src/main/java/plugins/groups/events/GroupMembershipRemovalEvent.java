package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;

/**
 * Event to indicating that person was removed from a group
 *
 * @author Shawn Hatch
 */
@Immutable
public record GroupMembershipRemovalEvent(PersonId personId,
										  GroupId groupId) implements Event {
}
