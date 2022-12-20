package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public record BatchAdditionEvent(BatchId batchId) implements Event {

	public BatchAdditionEvent {

	}

	@Override
	public String toString() {
		return "BatchAdditionEvent [batchId=" + batchId + "]";
	}

}
