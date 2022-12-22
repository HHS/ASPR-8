package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

@Immutable

public record StageImminentRemovalEvent(StageId stageId) implements Event {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StageImminentRemovalEvent [stageId=");
		builder.append(stageId);
		builder.append("]");
		return builder.toString();
	}


}
