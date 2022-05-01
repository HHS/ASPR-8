package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.errors.ContractException;

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

	private static enum LabelerId implements EventLabelerId {
		PRODUCER_RESOURCE, RESOURCE
	}

	private static void validateMaterialProducerId(SimulationContext simulationContext, MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class);
		if (!materialsDataManager.materialsProducerIdExists(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	private static void validateResourceId(SimulationContext simulationContext, ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		ResourcesDataManager resourcesDataManager = simulationContext.getDataManager(ResourcesDataManager.class);
		if (!resourcesDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID);
		}
	}

	public static EventLabel<MaterialsProducerResourceUpdateEvent> getEventLabelByMaterialsProducerAndResource(SimulationContext simulationContext, MaterialsProducerId materialsProducerId,
			ResourceId resourceId) {
		validateMaterialProducerId(simulationContext, materialsProducerId);
		validateResourceId(simulationContext, resourceId);
		return _getEventLabelByMaterialsProducerAndResource(materialsProducerId, resourceId);//

	}
	
	private static EventLabel<MaterialsProducerResourceUpdateEvent> _getEventLabelByMaterialsProducerAndResource(MaterialsProducerId materialsProducerId,
			ResourceId resourceId) {
		return EventLabel	.builder(MaterialsProducerResourceUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PRODUCER_RESOURCE)//
							.addKey(resourceId)//
							.addKey(materialsProducerId)//
							.build();//

	}


	public static EventLabeler<MaterialsProducerResourceUpdateEvent> getEventLabelerForMaterialsProducerAndResource() {
		return EventLabeler	.builder(MaterialsProducerResourceUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PRODUCER_RESOURCE)//
							.setLabelFunction((context, event) -> _getEventLabelByMaterialsProducerAndResource(event.getMaterialsProducerId(), event.getResourceId()))//
							.build();
	}

	public static EventLabel<MaterialsProducerResourceUpdateEvent> getEventLabelByResource(SimulationContext simulationContext, ResourceId resourceId) {
		validateResourceId(simulationContext, resourceId);
		return _getEventLabelByResource(resourceId);//
	}
	
	private static EventLabel<MaterialsProducerResourceUpdateEvent> _getEventLabelByResource(ResourceId resourceId) {
		
		return EventLabel	.builder(MaterialsProducerResourceUpdateEvent.class)//
							.setEventLabelerId(LabelerId.RESOURCE)//
							.addKey(resourceId)//
							.build();//
	}

	public static EventLabeler<MaterialsProducerResourceUpdateEvent> getEventLabelerForResource() {
		return EventLabeler	.builder(MaterialsProducerResourceUpdateEvent.class)//
							.setEventLabelerId(LabelerId.RESOURCE)//
							.setLabelFunction((context, event) -> _getEventLabelByResource(event.getResourceId()))//
							.build();
	}

	@Override
	public Object getPrimaryKeyValue() {
		return resourceId;
	}

}
