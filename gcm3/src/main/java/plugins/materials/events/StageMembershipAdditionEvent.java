package plugins.materials.events;

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
public class StageMembershipAdditionEvent implements Event {
	private final BatchId batchId;
	private final StageId stageId;

	public StageMembershipAdditionEvent(BatchId batchId, StageId stageId) {
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
		return "StageMembershipAdditionEvent [batchId=" + batchId + ", stageId=" + stageId + "]";
	}
	
	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<StageMembershipAdditionEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageMembershipAdditionEvent.class, LabelerId.ALL, StageMembershipAdditionEvent.class);

	public static EventLabel<StageMembershipAdditionEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMembershipAdditionEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageMembershipAdditionEvent.class, (context, event) -> ALL_LABEL);
	}

}
