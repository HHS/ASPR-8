package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

/**
 * Removes a batch from its current stage and moves it back into inventory.The batch must owned by the
 * invoking materials producer and its stage may not be in the offered state.
 *
 *
 */
@Immutable
public final class MoveBatchToInventoryEvent implements Event {
	private final BatchId batchId;

	
	public MoveBatchToInventoryEvent(BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

}
