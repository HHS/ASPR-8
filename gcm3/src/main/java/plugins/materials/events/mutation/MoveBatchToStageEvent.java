package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;

/**
 * Associates a batch with a stage, both of which are owned by the invoking
 * materials producer. The batch must be in the inventory of the invoking
 * materials producer and not associated with a stage and the stage must not be
 * in an offered state.
 *
 */
@Immutable
public final class MoveBatchToStageEvent implements Event {
	private final BatchId batchId;
	private final StageId stageId;

	
	public MoveBatchToStageEvent(BatchId batchId, StageId stageId) {
		super();
		this.batchId = batchId;
		this.stageId = stageId;
	}

	public BatchId getBatchId() {
		return batchId;
	}

	public StageId getStageId() {
		return stageId;
	}

}
