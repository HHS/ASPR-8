package gov.hhs.aspr.ms.gcm.plugins.groups.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a group type has been created
 *
 */
@Immutable
public record GroupTypeAdditionEvent(GroupTypeId groupTypeId) implements Event {
}
