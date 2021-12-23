package plugins.resources.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

/**
 * Event for transferring resource amounts between regions
 */
@Immutable
public final class InterRegionalResourceTransferEvent implements Event {

	private final ResourceId resourceId;

	private final RegionId sourceRegionId;

	private final RegionId destinationRegionId;

	private final long amount;

	/**
	 * Constructs the event
	 */
	public InterRegionalResourceTransferEvent(ResourceId resourceId, RegionId sourceRegionId, RegionId destinationRegionId, long amount) {
		this.resourceId = resourceId;
		this.sourceRegionId = sourceRegionId;
		this.destinationRegionId = destinationRegionId;
		this.amount = amount;
	}

	/**
	 * Returns the resource used to create this event
	 */
	public ResourceId getResourceId() {
		return resourceId;
	}

	/**
	 * Returns the source region used to create this event
	 */
	public RegionId getSourceRegionId() {
		return sourceRegionId;
	}

	/**
	 * Returns the destination region used to create this event
	 */
	public RegionId getDestinationRegionId() {
		return destinationRegionId;
	}

	/**
	 * Returns the resource amount used to create this event
	 */
	public long getAmount() {
		return amount;
	}
}
