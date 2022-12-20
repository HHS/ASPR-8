package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public record BatchAmountUpdateEvent(BatchId batchId, double previousAmount,
									 double currentAmount) implements Event {

	public BatchAmountUpdateEvent {

	}

	@Override
	public String toString() {
		return "BatchAmountUpdateEvent [batchId=" + batchId + ", previousAmount=" + previousAmount + ", currentAmount=" + currentAmount + "]";
	}

}
