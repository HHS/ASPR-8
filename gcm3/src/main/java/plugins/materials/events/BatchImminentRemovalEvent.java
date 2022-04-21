package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public class BatchImminentRemovalEvent implements Event {
	private final BatchId batchId;

	public BatchImminentRemovalEvent(final BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BatchImminentRemovalEvent [batchId=");
		builder.append(batchId);
		builder.append("]");
		return builder.toString();
	}

}
