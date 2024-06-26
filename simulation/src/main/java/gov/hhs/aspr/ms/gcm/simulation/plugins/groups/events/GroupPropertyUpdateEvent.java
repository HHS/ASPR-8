package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import net.jcip.annotations.Immutable;

/**
 * Event to indicating that a group had a property value change
 */
@Immutable
public record GroupPropertyUpdateEvent(GroupId groupId, GroupPropertyId groupPropertyId, Object previousPropertyValue,
		Object currentPropertyValue) implements Event {
}
