package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;

@Immutable
public record RegionPropertyUpdateEvent(RegionId regionId,
										RegionPropertyId regionPropertyId,
										Object previousPropertyValue,
										Object currentPropertyValue) implements Event {

	public RegionPropertyUpdateEvent {

	}

	@Override
	public String toString() {
		return "RegionPropertyUpdateEvent [regionId=" + regionId + ", regionPropertyId=" + regionPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}

}
