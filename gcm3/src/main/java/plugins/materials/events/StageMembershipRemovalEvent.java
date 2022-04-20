package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;

@Immutable
public class StageMembershipRemovalEvent implements Event {
	private final BatchId batchId;
	private final StageId stageId;

	public StageMembershipRemovalEvent(BatchId batchId, StageId stageId) {
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
		return "StageMembershipRemovalEvent [batchId=" + batchId + ", stageId=" + stageId + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageMembershipRemovalEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageMembershipRemovalEvent.class, LabelerId.ALL, StageMembershipRemovalEvent.class);

	public static EventLabel<StageMembershipRemovalEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMembershipRemovalEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(StageMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_LABEL)//
							.build();
	}
}
