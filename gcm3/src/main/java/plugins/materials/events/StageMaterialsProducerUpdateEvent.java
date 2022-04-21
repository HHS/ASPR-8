package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

@Immutable
public class StageMaterialsProducerUpdateEvent implements Event {
	private final StageId stageId;
	private final MaterialsProducerId previousMaterialsProducerId;
	private final MaterialsProducerId currentMaterialsProducerId;

	public StageMaterialsProducerUpdateEvent(StageId stageId, MaterialsProducerId previousMaterialsProducerId, MaterialsProducerId currentMaterialsProducerId) {
		super();
		this.stageId = stageId;
		this.previousMaterialsProducerId = previousMaterialsProducerId;
		this.currentMaterialsProducerId = currentMaterialsProducerId;
	}

	public StageId getStageId() {
		return stageId;
	}

	public MaterialsProducerId getPreviousMaterialsProducerId() {
		return previousMaterialsProducerId;
	}

	public MaterialsProducerId getCurrentMaterialsProducerId() {
		return currentMaterialsProducerId;
	}

	@Override
	public String toString() {
		return "StageMaterialsProducerUpdateEvent [stageId=" + stageId + ", previousMaterialsProducerId=" + previousMaterialsProducerId + ", currentMaterialsProducerId=" + currentMaterialsProducerId
				+ "]";
	}

	private static enum LabelerId implements EventLabelerId {
		SOURCE, DESTINATION, STAGE
	}

	private static void validateStageId(SimulationContext simulationContext, StageId stageId) {
		if (stageId == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class).get();
		if (!materialsDataManager.stageExists(stageId)) {
			throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID);
		}
	}

	private static void validateMaterialsProducerId(SimulationContext simulationContext, MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class).get();
		if (!materialsDataManager.materialsProducerIdExists(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelByDestination(SimulationContext simulationContext, MaterialsProducerId destinationMaterialsProducerId) {
		validateMaterialsProducerId(simulationContext, destinationMaterialsProducerId);
		return _getEventLabelByDestination(destinationMaterialsProducerId);
	}
	
	private static EventLabel<StageMaterialsProducerUpdateEvent> _getEventLabelByDestination(MaterialsProducerId destinationMaterialsProducerId) {
		return EventLabel	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DESTINATION)//
							.addKey(StageMaterialsProducerUpdateEvent.class).addKey(destinationMaterialsProducerId).build();
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForDestination() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DESTINATION)//
							.setLabelFunction((context, event) -> _getEventLabelByDestination(event.getCurrentMaterialsProducerId())).build();
	}

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelBySource(SimulationContext simulationContext, MaterialsProducerId sourceMaterialsProducerId) {
		validateMaterialsProducerId(simulationContext, sourceMaterialsProducerId);
		return _getEventLabelBySource(sourceMaterialsProducerId);//
	}
	
	private static EventLabel<StageMaterialsProducerUpdateEvent> _getEventLabelBySource(MaterialsProducerId sourceMaterialsProducerId) {
		return EventLabel	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.SOURCE)//
							.addKey(StageMaterialsProducerUpdateEvent.class)//
							.addKey(sourceMaterialsProducerId)//
							.build();//
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForSource() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class).setEventLabelerId(LabelerId.SOURCE)//
							.setLabelFunction((context, event) -> _getEventLabelBySource(event.getPreviousMaterialsProducerId()))//
							.build();
	}

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelByStage(SimulationContext simulationContext, StageId stageId) {
		validateStageId(simulationContext, stageId);
		return _getEventLabelByStage(stageId);
	}
	
	private static EventLabel<StageMaterialsProducerUpdateEvent> _getEventLabelByStage(StageId stageId) {
		
		return EventLabel	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.STAGE)//
							.addKey(StageMaterialsProducerUpdateEvent.class)//
							.addKey(stageId)//
							.build();
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForStage() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.STAGE)//
							.setLabelFunction((context, event) -> _getEventLabelByStage(event.getStageId()))//
							.build();
	}

}
