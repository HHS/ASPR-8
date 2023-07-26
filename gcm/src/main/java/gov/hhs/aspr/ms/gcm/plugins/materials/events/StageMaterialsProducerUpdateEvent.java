package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import net.jcip.annotations.Immutable;

@Immutable
public record StageMaterialsProducerUpdateEvent(StageId stageId,
												MaterialsProducerId previousMaterialsProducerId,
												MaterialsProducerId currentMaterialsProducerId) implements Event {
}
