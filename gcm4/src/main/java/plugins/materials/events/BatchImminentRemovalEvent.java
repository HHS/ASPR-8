package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public record BatchImminentRemovalEvent(BatchId batchId) implements Event {


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchImminentRemovalEvent [batchId=");
		builder.append(batchId);
		builder.append("]");
		return builder.toString();
	}

}
