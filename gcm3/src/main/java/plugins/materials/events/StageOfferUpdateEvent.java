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
import plugins.materials.support.StageId;

@Immutable
public class StageOfferUpdateEvent implements Event {
	private final StageId stageId;
	private final boolean previousOfferState;
	private final boolean currentOfferState;

	public StageOfferUpdateEvent(StageId stageId, boolean previousOfferState, boolean currentOfferState) {
		super();
		this.stageId = stageId;
		this.previousOfferState = previousOfferState;
		this.currentOfferState = currentOfferState;
	}

	public StageId getStageId() {
		return stageId;
	}

	public boolean isPreviousOfferState() {
		return previousOfferState;
	}

	public boolean isCurrentOfferState() {
		return currentOfferState;
	}

	@Override
	public String toString() {
		return "StageOfferUpdateEvent [stageId=" + stageId + ", previousOfferState=" + previousOfferState + ", currentOfferState=" + currentOfferState + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		STAGE, ALL
	}

	private static void validateStageId(SimulationContext simulationContext, StageId stageId) {
		if (stageId == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class).get();
		if (!materialsDataManager.stageExists(stageId)) {
			throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	public static EventLabel<StageOfferUpdateEvent> getEventLabelByStage(SimulationContext simulationContext, StageId stageId) {
		validateStageId(simulationContext, stageId);
		return new EventLabel<>(StageOfferUpdateEvent.class, LabelerId.STAGE, StageOfferUpdateEvent.class, stageId);
	}

	public static EventLabeler<StageOfferUpdateEvent> getEventLabelerForStage() {
		return EventLabeler	.builder(StageOfferUpdateEvent.class)//
							.setEventLabelerId(LabelerId.STAGE).setLabelFunction((context, event) -> getEventLabelByStage(context, event.getStageId()))//
							.build();
	}

	private final static EventLabel<StageOfferUpdateEvent> ALL_LABEL = new EventLabel<>(StageOfferUpdateEvent.class, LabelerId.ALL, StageOfferUpdateEvent.class);

	public static EventLabel<StageOfferUpdateEvent> getEventLabelByAll(SimulationContext simulationContext) {
		return ALL_LABEL;
	}

	public static EventLabeler<StageOfferUpdateEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(StageOfferUpdateEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_LABEL)//
							.build();
	}

}
