package gov.hhs.aspr.ms.gcm.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a resource property has changed.
 *
 */
@Immutable
public record ResourcePropertyUpdateEvent(ResourceId resourceId,
										  ResourcePropertyId resourcePropertyId,
										  Object previousPropertyValue,
										  Object currentPropertyValue) implements Event {
}
