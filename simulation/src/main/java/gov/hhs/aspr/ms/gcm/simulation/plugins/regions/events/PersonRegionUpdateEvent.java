package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import net.jcip.annotations.Immutable;

@Immutable
public record PersonRegionUpdateEvent(PersonId personId, RegionId previousRegionId, RegionId currentRegionId)
		implements Event {
}
