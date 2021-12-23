package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

/**
 * Transfers an amount of resource from a materials producer to a region.
 */
@Immutable
public final class ProducedResourceTransferToRegionEvent implements Event {

	private final MaterialsProducerId materialsProducerId;

	private final ResourceId resourceId;

	private final RegionId regionId;

	private final long amount;

	
	public ProducedResourceTransferToRegionEvent(MaterialsProducerId materialsProducerId, ResourceId resourceId, RegionId regionId, long amount) {
		super();
		this.materialsProducerId = materialsProducerId;
		this.resourceId = resourceId;
		this.regionId = regionId;
		this.amount = amount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProducedResourceTransferToRegionEvent [materialsProducerId=");
		builder.append(materialsProducerId);
		builder.append(", resourceId=");
		builder.append(resourceId);
		builder.append(", regionId=");
		builder.append(regionId);
		builder.append(", amount=");
		builder.append(amount);
		builder.append("]");
		return builder.toString();
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	public long getAmount() {
		return amount;
	}

}
