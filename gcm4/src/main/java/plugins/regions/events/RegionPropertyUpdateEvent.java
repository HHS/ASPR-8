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
}
