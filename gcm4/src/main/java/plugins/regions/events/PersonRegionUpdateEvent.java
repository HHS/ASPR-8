package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;

@Immutable
public record PersonRegionUpdateEvent(PersonId personId,
									  RegionId previousRegionId,
									  RegionId currentRegionId) implements Event {

	@Override
	public String toString() {
		return "PersonRegionUpdateEvent [personId=" + personId + ", previousRegionId=" + previousRegionId + ", currentRegionId=" + currentRegionId + "]";
	}

}
