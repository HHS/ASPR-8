package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * Event to signal the imminent removal of a group from the simulation
 *
 * @author Shawn Hatch
 */

@Immutable
public record GroupImminentRemovalEvent(GroupId groupId) implements Event {
	/**
	 * Constructs this event from the group id
	 */
	public GroupImminentRemovalEvent {
	}

}
