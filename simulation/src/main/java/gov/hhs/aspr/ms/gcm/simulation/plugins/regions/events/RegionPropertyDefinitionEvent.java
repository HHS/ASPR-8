package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import net.jcip.annotations.Immutable;

/**
 * Event indicating the addition of a region property
 */
@Immutable
public record RegionPropertyDefinitionEvent(RegionPropertyId regionPropertyId) implements Event {
}
