package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;

/**
 * Event to indicating that a group had a property value change
 *
 * @author Shawn Hatch
 */
@Immutable
public record GroupPropertyUpdateEvent(GroupId groupId,
									   GroupPropertyId groupPropertyId,
									   Object previousPropertyValue,
									   Object currentPropertyValue) implements Event {
	/**
	 * Constructs this event from the given group id, group property id ,
	 * previous property value and current property value.
	 */
	public GroupPropertyUpdateEvent {
	}

}
