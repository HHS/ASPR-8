package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import net.jcip.annotations.Immutable;

@Immutable
public record BatchAdditionEvent(BatchId batchId) implements Event {
}
