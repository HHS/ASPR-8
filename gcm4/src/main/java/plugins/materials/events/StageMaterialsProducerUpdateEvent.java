package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

@Immutable
public record StageMaterialsProducerUpdateEvent(StageId stageId,
												MaterialsProducerId previousMaterialsProducerId,
												MaterialsProducerId currentMaterialsProducerId) implements Event {

	@Override
	public String toString() {
		return "StageMaterialsProducerUpdateEvent [stageId=" + stageId + ", previousMaterialsProducerId=" + previousMaterialsProducerId + ", currentMaterialsProducerId=" + currentMaterialsProducerId
				+ "]";
	}

}
