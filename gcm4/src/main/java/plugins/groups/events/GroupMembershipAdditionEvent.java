package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.people.support.PersonId;

/**
 * Event to indicating that person was added to a group
 *
 * @author Shawn Hatch
 */

@Immutable
public record GroupMembershipAdditionEvent(PersonId personId,
										   GroupId groupId) implements Event {
}
