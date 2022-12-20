package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

@Immutable
public record StageAdditionEvent(StageId stageId) implements Event {

	public StageAdditionEvent {

	}

	@Override
	public String toString() {
		return "StageCreation [stageId=" + stageId + "]";
	}


}
