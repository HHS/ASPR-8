package plugins.groups.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;

/**
 * Event for setting a group property value.
 *
 */
@Immutable
public final class GroupPropertyValueAssignmentEvent implements Event {

	private final GroupId groupId;

	private final GroupPropertyId groupPropertyId;

	private Object groupPropertyValue;

	/**
	 * Constructs the event
	 * 
	 */
	public GroupPropertyValueAssignmentEvent(GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue) {
		super();
		this.groupId = groupId;
		this.groupPropertyId = groupPropertyId;
		this.groupPropertyValue = groupPropertyValue;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

	/**
	 * Returns the group property id used to create this event
	 */
	public GroupPropertyId getGroupPropertyId() {
		return groupPropertyId;
	}

	/**
	 * Returns the group property value used to create this event
	 */
	public Object getGroupPropertyValue() {
		return groupPropertyValue;
	}

}
