package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import net.jcip.annotations.Immutable;

@Immutable
public record StageMembershipRemovalEvent(BatchId batchId, StageId stageId) implements Event {
}
