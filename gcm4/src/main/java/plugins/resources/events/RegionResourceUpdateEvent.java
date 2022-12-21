package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

/**
 * An observation event indicating that a region's resource level has changed.
 *
 * @author Shawn Hatch
 */

@Immutable
public record RegionResourceUpdateEvent(RegionId regionId,
										ResourceId resourceId, long previousResourceLevel,
										long currentResourceLevel) implements Event {
	/**
	 * Constructs the event
	 */
	public RegionResourceUpdateEvent {
	}

}
