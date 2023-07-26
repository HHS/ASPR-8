package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import net.jcip.annotations.Immutable;

@Immutable
public record BatchAmountUpdateEvent(BatchId batchId, double previousAmount,
									 double currentAmount) implements Event {
}
