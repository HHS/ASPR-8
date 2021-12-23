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
public class StageMembershipRemovalObservationEvent implements Event {
	private final BatchId batchId;
	private final StageId stageId;

	public StageMembershipRemovalObservationEvent(BatchId batchId, StageId stageId) {
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
		return "StageMembershipRemovalObservationEvent [batchId=" + batchId + ", stageId=" + stageId + "]";
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageMembershipRemovalObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageMembershipRemovalObservationEvent.class, LabelerId.ALL, StageMembershipRemovalObservationEvent.class);

	public static EventLabel<StageMembershipRemovalObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMembershipRemovalObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageMembershipRemovalObservationEvent.class, (context, event) -> ALL_LABEL);
	}
}
