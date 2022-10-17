package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * An event indicating that a group has been created
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupAdditionEvent implements Event {

	private final GroupId groupId;

	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupAdditionEvent(final GroupId groupId) {
		this.groupId = groupId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

}
