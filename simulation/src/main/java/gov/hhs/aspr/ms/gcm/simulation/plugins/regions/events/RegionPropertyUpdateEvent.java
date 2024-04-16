package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import net.jcip.annotations.Immutable;

@Immutable
public record RegionPropertyUpdateEvent(RegionId regionId, RegionPropertyId regionPropertyId,
		Object previousPropertyValue, Object currentPropertyValue) implements Event {
}
