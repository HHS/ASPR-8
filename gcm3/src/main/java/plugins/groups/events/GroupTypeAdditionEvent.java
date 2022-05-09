package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupTypeId;

/**
 * An event indicating that a group type has been created
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupTypeAdditionEvent implements Event {

	private final GroupTypeId groupTypeId;

	/**
	 * Constructs this event from the group type id
	 * 
	 */
	public GroupTypeAdditionEvent(final GroupTypeId groupTypeId) {
		this.groupTypeId = groupTypeId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupTypeId getGroupTypeId() {
		return groupTypeId;
	}

}
