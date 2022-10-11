package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

/**
 * An observation event indicating that a region's resource level has changed.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class RegionResourceUpdateEvent implements Event {
	private final RegionId regionId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	/**
	 * Constructs the event
	 */
	public RegionResourceUpdateEvent(RegionId regionId, ResourceId resourceId, long previousResourceLevel, long currentResourceLevel) {
		super();
		this.regionId = regionId;
		this.resourceId = resourceId;
		this.previousResourceLevel = previousResourceLevel;
		this.currentResourceLevel = currentResourceLevel;
	}

	/**
	 * Returns the resource id used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the region id used to create this event
	 */
	public RegionId getRegionId() {
		return regionId;
	}

	/**
	 * Returns the previous resource level used to create this event
	 */
	public long getPreviousResourceLevel() {
		return previousResourceLevel;
	}

	/**
	 * Returns the current resource level used to create this event
	 */
	public long getCurrentResourceLevel() {
		return currentResourceLevel;
	}
	
	/**
	 * Returns the resource id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
