package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;

/**
 * Destroys a stage owned by the invoking materials producer component. If
 * destroyBatches is set to true, then all batches associated with the stage are
 * also destroyed, otherwise they are returned to inventory.
 *
 *
 */
@Immutable
public final class StageRemovalRequestEvent implements Event {
	private final StageId stageId;
	private final boolean destroyBatches;

	
	public StageRemovalRequestEvent(StageId stageId, boolean destroyBatches) {
		super();
		this.stageId = stageId;
		this.destroyBatches = destroyBatches;
	}

	public StageId getStageId() {
		return stageId;
	}

	public boolean isDestroyBatches() {
		return destroyBatches;
	}

}
