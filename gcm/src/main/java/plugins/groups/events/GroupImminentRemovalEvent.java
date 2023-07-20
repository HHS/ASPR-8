package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * Event to signal the imminent removal of a group from the simulation
 *
 */

@Immutable
public record GroupImminentRemovalEvent(GroupId groupId) implements Event {
}
