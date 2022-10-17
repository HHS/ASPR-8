package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;

/**
 * Event to indicating that a group had a property value change
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupPropertyUpdateEvent implements Event {
	private final GroupId groupId;
	private final GroupPropertyId groupPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs this event from the given group id, group property id ,
	 * previous property value and current property value.
	 * 
	 */
	public GroupPropertyUpdateEvent(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
		super();
		this.groupId = groupId;
		this.groupPropertyId = groupPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the current property value id used to create this event
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
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
	 * Returns the previous property value id used to create this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

}
