package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.StageId;
import plugins.resources.support.ResourceId;

/**
 * Converts a stage to a resource that will be held in the inventory of the
 * invoking materials producer. The stage and its associated batches are
 * destroyed. The stage must be owned by the invoking materials producer and
 * must not be in the offered state.
 *
 */
@Immutable
public final class StageToResourceConversionEvent implements Event {
	private final StageId stageId;

	private final ResourceId resourceId;

	private final long amount;

	
	public StageToResourceConversionEvent(StageId stageId, ResourceId resourceId, long amount) {
		super();
		this.stageId = stageId;
		this.resourceId = resourceId;
		this.amount = amount;
	}

	public StageId getStageId() {
		return stageId;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public long getAmount() {
		return amount;
	}

}
