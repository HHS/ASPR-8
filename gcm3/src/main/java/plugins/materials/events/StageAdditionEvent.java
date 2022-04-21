package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

@Immutable
public class StageAdditionEvent implements Event {
	private final StageId stageId;

	public StageAdditionEvent(StageId stageId) {
		super();
		this.stageId = stageId;
	}

	public StageId getStageId() {
		return stageId;
	}

	@Override
	public String toString() {
		return "StageCreation [stageId=" + stageId + "]";
	}

	
}
