package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;

@Immutable
public class MaterialsProducerResourceChangeObservationEvent implements Event {
	private final MaterialsProducerId materialsProducerId;
	private final ResourceId resourceId;
	private final long previousResourceLevel;
	private final long currentResourceLevel;

	public MaterialsProducerResourceChangeObservationEvent(MaterialsProducerId materialsProducerId, ResourceId resourceId, long previousResourceLevel, long currentResourceLevel) {
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
		return "MaterialsProducerResourceChangeObservationEvent [materialsProducerId=" + materialsProducerId + ", resourceId=" + resourceId + ", previousResourceLevel=" + previousResourceLevel + ", currentResourceLevel=" + currentResourceLevel + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PRODUCER_RESOURCE, RESOURCE
	}

	private static void validateMaterialProducerId(SimulationContext simulationContext, MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataView materialsDataView = simulationContext.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.materialsProducerIdExists(materialsProducerId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	private static void validateResourceId(SimulationContext simulationContext, ResourceId resourceId) {
		if (resourceId == null) {
			simulationContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourceDataView resourceDataView = simulationContext.getDataView(ResourceDataView.class).get();
		if (!resourceDataView.resourceIdExists(resourceId)) {
			simulationContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID);
		}
	}

	public static EventLabel<MaterialsProducerResourceChangeObservationEvent> getEventLabelByMaterialsProducerAndResource(SimulationContext simulationContext, MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		validateMaterialProducerId(simulationContext, materialsProducerId);
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.PRODUCER_RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, materialsProducerId, resourceId);
	}

	public static EventLabeler<MaterialsProducerResourceChangeObservationEvent> getEventLabelerForMaterialsProducerAndResource() {
		return new SimpleEventLabeler<>(LabelerId.PRODUCER_RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.PRODUCER_RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, event.getMaterialsProducerId(), event.getResourceId()));
	}

	public static EventLabel<MaterialsProducerResourceChangeObservationEvent> getEventLabelByResource(SimulationContext simulationContext, ResourceId resourceId) {
		validateResourceId(simulationContext, resourceId);
		return new MultiKeyEventLabel<>(resourceId, LabelerId.RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, resourceId);
	}

	public static EventLabeler<MaterialsProducerResourceChangeObservationEvent> getEventLabelerForResource() {
		return new SimpleEventLabeler<>(LabelerId.RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getResourceId(), LabelerId.RESOURCE, MaterialsProducerResourceChangeObservationEvent.class, event.getResourceId()));
	}

	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
