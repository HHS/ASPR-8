package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupConstructionInfo;

/**
 * 
 * Creates a group from a {@link GroupConstructionInfo}
 *
 */
@Immutable
public final class GroupConstructionEvent implements Event {
	private final GroupConstructionInfo groupConstructionInfo;

	/**
	 * 
	 * Returns this group construction event 
	 */
	public GroupConstructionEvent(GroupConstructionInfo groupConstructionInfo) {
		this.groupConstructionInfo = groupConstructionInfo;
	}

	/**
	 * Returns the group construction info used to create this event
	 */
	public GroupConstructionInfo getGroupConstructionInfo() {
		return groupConstructionInfo;
	}

}
