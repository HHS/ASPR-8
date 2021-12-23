package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupTypeId;

/**
 * Creates a new group from the {@link GroupTypeId}
 *
 */
@Immutable
public final class GroupCreationEvent implements Event {
	private final GroupTypeId groupTypeId;
	
	public GroupCreationEvent(GroupTypeId groupTypeId) {		
		this.groupTypeId = groupTypeId;
	}

	public GroupTypeId getGroupTypeId() {
		return groupTypeId;
	}

}
