package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialId;
import plugins.materials.support.StageId;

/**
 * Converts a stage to a batch that will be held in the inventory of the
 * invoking materials producer. The stage and its associated batches are
 * destroyed. The stage must be owned by the invoking materials producer and
 * must not be in the offered state.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class StageToBatchConversionEvent implements Event {

	private final StageId stageId;

	private final MaterialId materialId;

	private final double amount;

	
	public StageToBatchConversionEvent(StageId stageId, MaterialId materialId, double amount) {
		super();
		this.stageId = stageId;
		this.materialId = materialId;
		this.amount = amount;
	}

	public StageId getStageId() {
		return stageId;
	}

	public MaterialId getMaterialId() {
		return materialId;
	}

	public double getAmount() {
		return amount;
	}

}
