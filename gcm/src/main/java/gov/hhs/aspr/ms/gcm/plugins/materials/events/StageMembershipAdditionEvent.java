package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import net.jcip.annotations.Immutable;

@Immutable
public record StageMembershipAdditionEvent(BatchId batchId, StageId stageId) implements Event {
}
