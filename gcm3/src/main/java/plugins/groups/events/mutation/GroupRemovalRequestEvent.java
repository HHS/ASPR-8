package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;

/**
 * Requests that a group be removed from the simulation. All people associated
 * with the group will remain in the simulation.
 *
 */
@Immutable
public final class GroupRemovalRequestEvent implements Event {
	
	private final GroupId groupId;

	/**
	 * Constructs this event from the given group id
	 */
	public GroupRemovalRequestEvent(GroupId groupId) {		
		this.groupId = groupId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

}
