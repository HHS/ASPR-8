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
}
