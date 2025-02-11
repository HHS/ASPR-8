package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchPropertyId;
import net.jcip.annotations.Immutable;

@Immutable
public record BatchPropertyUpdateEvent(BatchId batchId, BatchPropertyId batchPropertyId, Object previousPropertyValue,
		Object currentPropertyValue) implements Event {
}
