package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a group has been created
 *
 */
@Immutable
public record GroupAdditionEvent(GroupId groupId) implements Event {
}
