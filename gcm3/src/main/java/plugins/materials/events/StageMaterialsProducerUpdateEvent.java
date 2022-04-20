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
		SOURCE, DESTINATION, STAGE, ALL
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
		return new EventLabel<>(StageMaterialsProducerUpdateEvent.class, LabelerId.DESTINATION, StageMaterialsProducerUpdateEvent.class, destinationMaterialsProducerId);
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForDestination() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.DESTINATION)//
							.setLabelFunction((context, event) -> getEventLabelByDestination(context, event.getCurrentMaterialsProducerId())).build();
	}

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelBySource(SimulationContext simulationContext, MaterialsProducerId sourceMaterialsProducerId) {
		validateMaterialsProducerId(simulationContext, sourceMaterialsProducerId);
		return new EventLabel<>(StageMaterialsProducerUpdateEvent.class, LabelerId.SOURCE, StageMaterialsProducerUpdateEvent.class, sourceMaterialsProducerId);
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForSource() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class).setEventLabelerId(LabelerId.SOURCE)//
							.setLabelFunction((context, event) -> getEventLabelBySource(context, event.getPreviousMaterialsProducerId()))//
							.build();
	}

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelByStage(SimulationContext simulationContext, StageId stageId) {
		validateStageId(simulationContext, stageId);
		return new EventLabel<>(StageMaterialsProducerUpdateEvent.class, LabelerId.STAGE, StageMaterialsProducerUpdateEvent.class, stageId);
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForStage() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.STAGE)//
							.setLabelFunction((context, event) -> getEventLabelByStage(context, event.getStageId()))//
							.build();
	}

	private final static EventLabel<StageMaterialsProducerUpdateEvent> ALL_LABEL = new EventLabel<>(StageMaterialsProducerUpdateEvent.class, LabelerId.ALL,
			StageMaterialsProducerUpdateEvent.class);

	public static EventLabel<StageMaterialsProducerUpdateEvent> getEventLabelByAll(SimulationContext simulationContext) {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMaterialsProducerUpdateEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(StageMaterialsProducerUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_LABEL).build();
	}

}
