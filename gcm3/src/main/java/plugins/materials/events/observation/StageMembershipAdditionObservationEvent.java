package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;

@Immutable
public class StageMembershipAdditionObservationEvent implements Event {
	private final BatchId batchId;
	private final StageId stageId;

	public StageMembershipAdditionObservationEvent(BatchId batchId, StageId stageId) {
		super();
		this.batchId = batchId;
		this.stageId = stageId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		return "StageMembershipAdditionObservationEvent [batchId=" + batchId + ", stageId=" + stageId + "]";
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageMembershipAdditionObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageMembershipAdditionObservationEvent.class, LabelerId.ALL, StageMembershipAdditionObservationEvent.class);

	public static EventLabel<StageMembershipAdditionObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMembershipAdditionObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageMembershipAdditionObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
