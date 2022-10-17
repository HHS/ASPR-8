package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
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

}
