package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import net.jcip.annotations.Immutable;

@Immutable
public record PersonRegionUpdateEvent(PersonId personId,
									  RegionId previousRegionId,
									  RegionId currentRegionId) implements Event {
}
