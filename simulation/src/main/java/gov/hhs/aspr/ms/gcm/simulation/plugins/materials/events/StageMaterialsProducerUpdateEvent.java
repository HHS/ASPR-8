package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import net.jcip.annotations.Immutable;

@Immutable
public record StageMaterialsProducerUpdateEvent(StageId stageId, MaterialsProducerId previousMaterialsProducerId,
		MaterialsProducerId currentMaterialsProducerId) implements Event {
}
