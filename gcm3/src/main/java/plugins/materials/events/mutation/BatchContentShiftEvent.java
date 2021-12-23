package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

/**
 * Transfers the given amount from one batch to another. The batches must be
 * distinct and owned by the invoking materials producer component and neither
 * may be part of an offered stage.
 *
 */
@Immutable
public final class BatchContentShiftEvent implements Event {

	private final BatchId sourceBatchId;

	private final BatchId destinationBatchId;

	private final double amount;

	
	public BatchContentShiftEvent(BatchId sourceBatchId, BatchId destinationBatchId, double amount) {
		super();
		this.sourceBatchId = sourceBatchId;
		this.destinationBatchId = destinationBatchId;
		this.amount = amount;
	}

	public BatchId getSourceBatchId() {
		return sourceBatchId;
	}

	public BatchId getDestinationBatchId() {
		return destinationBatchId;
	}

	public double getAmount() {
		return amount;
	}

}
