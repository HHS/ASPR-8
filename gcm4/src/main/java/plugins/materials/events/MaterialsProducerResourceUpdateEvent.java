package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.resources.support.ResourceId;

@Immutable
public class MaterialsProducerResourceUpdateEvent implements Event {
	private final MaterialsProducerId materialsProducerId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	public MaterialsProducerResourceUpdateEvent(MaterialsProducerId materialsProducerId, ResourceId resourceId, long previousResourceLevel, long currentResourceLevel) {
		super();
		this.materialsProducerId = materialsProducerId;
		this.resourceId = resourceId;
		this.previousResourceLevel = previousResourceLevel;
		this.currentResourceLevel = currentResourceLevel;
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public long getPreviousResourceLevel() {
		return previousResourceLevel;
	}

	public long getCurrentResourceLevel() {
		return currentResourceLevel;
	}

	@Override
	public String toString() {
		return "MaterialsProducerResourceUpdateEvent [materialsProducerId=" + materialsProducerId + ", resourceId=" + resourceId + ", previousResourceLevel=" + previousResourceLevel
				+ ", currentResourceLevel=" + currentResourceLevel + "]";
	}

}
