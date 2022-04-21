package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public class BatchAdditionEvent implements Event {
	private final BatchId batchId;

	public BatchAdditionEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		return "BatchAdditionEvent [batchId=" + batchId + "]";
	}

}
