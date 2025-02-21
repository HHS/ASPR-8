package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourcePropertyId;
import net.jcip.annotations.Immutable;

@Immutable
public record ResourcePropertyDefinitionEvent(ResourceId resourceId, ResourcePropertyId resourcePropertyId,
		Object resourcePropertyValue) implements Event {
}
