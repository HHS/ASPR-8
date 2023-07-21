package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * An event indicating that a group has been created
 *
 */
@Immutable
public record GroupAdditionEvent(GroupId groupId) implements Event {
}
