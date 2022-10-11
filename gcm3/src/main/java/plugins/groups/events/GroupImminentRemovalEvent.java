package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * Event to signal the imminent removal of a group from the simulation
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GroupImminentRemovalEvent implements Event {
	private final GroupId groupId;

	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupImminentRemovalEvent(final GroupId groupId) {
		super();
		this.groupId = groupId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

}
