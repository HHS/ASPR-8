package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

@Immutable
public class BatchAmountUpdateEvent implements Event {
	private final BatchId batchId;
	private final double previousAmount;
	private final double currentAmount;

	public BatchAmountUpdateEvent(BatchId batchId, double previousAmount, double currentAmount) {
		super();
		this.batchId = batchId;
		this.previousAmount = previousAmount;
		this.currentAmount = currentAmount;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public double getPreviousAmount() {
		return previousAmount;
	}

	public double getCurrentAmount() {
		return currentAmount;
	}

	@Override
	public String toString() {
		return "BatchAmountUpdateEvent [batchId=" + batchId + ", previousAmount=" + previousAmount + ", currentAmount=" + currentAmount + "]";
	}

}
