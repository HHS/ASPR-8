package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

@Immutable
public class StageMaterialsProducerUpdateEvent implements Event {
	private final StageId stageId;
	private final MaterialsProducerId previousMaterialsProducerId;
	private final MaterialsProducerId currentMaterialsProducerId;

	public StageMaterialsProducerUpdateEvent(StageId stageId, MaterialsProducerId previousMaterialsProducerId, MaterialsProducerId currentMaterialsProducerId) {
		super();
		this.stageId = stageId;
		this.previousMaterialsProducerId = previousMaterialsProducerId;
		this.currentMaterialsProducerId = currentMaterialsProducerId;
	}

	public StageId getStageId() {
		return stageId;
	}

	public MaterialsProducerId getPreviousMaterialsProducerId() {
		return previousMaterialsProducerId;
	}

	public MaterialsProducerId getCurrentMaterialsProducerId() {
		return currentMaterialsProducerId;
	}

	@Override
	public String toString() {
		return "StageMaterialsProducerUpdateEvent [stageId=" + stageId + ", previousMaterialsProducerId=" + previousMaterialsProducerId + ", currentMaterialsProducerId=" + currentMaterialsProducerId
				+ "]";
	}

}
