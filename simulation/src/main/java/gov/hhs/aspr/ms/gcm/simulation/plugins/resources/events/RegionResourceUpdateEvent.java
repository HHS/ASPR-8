package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;

/**
 * An observation event indicating that a region's resource level has changed.
 */
@Immutable
public record RegionResourceUpdateEvent(RegionId regionId, ResourceId resourceId, long previousResourceLevel,
		long currentResourceLevel) implements Event {
}
