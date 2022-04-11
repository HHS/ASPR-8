package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.StageId;

@Immutable
public class StageOfferChangeObservationEvent implements Event {
	private final StageId stageId;
	private final boolean previousOfferState;
	private final boolean currentOfferState;

	public StageOfferChangeObservationEvent(StageId stageId, boolean previousOfferState, boolean currentOfferState) {
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
		return "StageOfferChangeObservationEvent [stageId=" + stageId + ", previousOfferState=" + previousOfferState + ", currentOfferState=" + currentOfferState + "]";
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

	public static EventLabel<StageOfferChangeObservationEvent> getEventLabelByStage(SimulationContext simulationContext, StageId stageId) {
		validateStageId(simulationContext, stageId);
		return new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.STAGE, StageOfferChangeObservationEvent.class, stageId);
	}

	public static EventLabeler<StageOfferChangeObservationEvent> getEventLabelerForStage() {
		return new SimpleEventLabeler<>(LabelerId.STAGE, StageOfferChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.STAGE, StageOfferChangeObservationEvent.class, event.getStageId()));
	}

	private final static EventLabel<StageOfferChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.ALL, StageOfferChangeObservationEvent.class);

	public static EventLabel<StageOfferChangeObservationEvent> getEventLabelByAll(SimulationContext simulationContext) {
		return ALL_LABEL;
	}

	public static EventLabeler<StageOfferChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageOfferChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
