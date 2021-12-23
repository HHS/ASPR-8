package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;

/**
 * Destroys the indicated batch that is owned by the invoking materials
 * producer. The batch may not be part of an offered stage.
 *
 */
@Immutable
public final class BatchRemovalRequestEvent implements Event {
	private final BatchId batchId;

	
	public BatchRemovalRequestEvent(BatchId batchId) {
		super();
		this.batchId = batchId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

}
