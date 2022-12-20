package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * An event indicating that a group has been created
 *
 * @author Shawn Hatch
 */
@Immutable
public record GroupAdditionEvent(GroupId groupId) implements Event {

	/**
	 * Constructs this event from the group id
	 */
	public GroupAdditionEvent {
	}

}
