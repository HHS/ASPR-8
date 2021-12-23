package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.datacontainers.MaterialsDataView;
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

	private static void validateStageId(Context context, StageId stageId) {
		if (stageId == null) {
			context.throwContractException(MaterialsError.NULL_STAGE_ID);
		}
		MaterialsDataView materialsDataView = context.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.stageExists(stageId)) {
			context.throwContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	public static EventLabel<StageOfferChangeObservationEvent> getEventLabelByStage(Context context, StageId stageId) {
		validateStageId(context, stageId);
		return new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.STAGE, StageOfferChangeObservationEvent.class, stageId);
	}

	public static EventLabeler<StageOfferChangeObservationEvent> getEventLabelerForStage() {
		return new SimpleEventLabeler<>(LabelerId.STAGE, StageOfferChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.STAGE, StageOfferChangeObservationEvent.class, event.getStageId()));
	}

	private final static EventLabel<StageOfferChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageOfferChangeObservationEvent.class, LabelerId.ALL, StageOfferChangeObservationEvent.class);

	public static EventLabel<StageOfferChangeObservationEvent> getEventLabelByAll(Context context) {
		return ALL_LABEL;
	}

	public static EventLabeler<StageOfferChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageOfferChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
