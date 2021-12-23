package plugins.resources.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

/**
 * Event for removing an amount of resource from a region.
 */
@Immutable
public final class RegionResourceRemovalEvent implements Event {

	private final ResourceId resourceId;

	private final RegionId regionId;

	private final long amount;

	/**
	 * Constructs the event 
	 */
	public RegionResourceRemovalEvent(ResourceId resourceId, RegionId regionId, long amount) {
		super();
		this.resourceId = resourceId;
		this.regionId = regionId;
		this.amount = amount;
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
	 * Returns the resource amount used to create this event
	 */
	public long getAmount() {
		return amount;
	}

}
