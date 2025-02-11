package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a resource id has been added.
 */
@Immutable
public record ResourceIdAdditionEvent(ResourceId resourceId, boolean timeTrackingPolicy) implements Event {
}
