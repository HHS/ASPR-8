package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

@Immutable

public class StageImminentRemovalEvent implements Event {
	private final StageId stageId;

	public StageImminentRemovalEvent(StageId stageId) {
		super();
		this.stageId = stageId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StageImminentRemovalEvent [stageId=");
		builder.append(stageId);
		builder.append("]");
		return builder.toString();
	}

	
}
