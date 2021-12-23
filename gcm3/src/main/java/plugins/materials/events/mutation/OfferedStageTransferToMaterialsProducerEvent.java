package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

/**
 * Transfers an offered stage to the provided materials producer. Once
 * transferred, the stage will not be in the offered state.
 *
 */
@Immutable
public final class OfferedStageTransferToMaterialsProducerEvent implements Event {

	private final StageId stageId;

	private final MaterialsProducerId materialsProducerId;

	
	public OfferedStageTransferToMaterialsProducerEvent(StageId stageId, MaterialsProducerId materialsProducerId) {
		super();
		this.stageId = stageId;
		this.materialsProducerId = materialsProducerId;
	}

	public StageId getStageId() {
		return stageId;
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

}
