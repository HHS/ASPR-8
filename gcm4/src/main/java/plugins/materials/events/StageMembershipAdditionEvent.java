package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;

@Immutable
public record StageMembershipAdditionEvent(BatchId batchId,
										   StageId stageId) implements Event {

	@Override
	public String toString() {
		return "StageMembershipAdditionEvent [batchId=" + batchId + ", stageId=" + stageId + "]";
	}

}
