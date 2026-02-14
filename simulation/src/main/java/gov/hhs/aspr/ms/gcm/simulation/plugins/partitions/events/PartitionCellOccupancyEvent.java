package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.LabelSet;
import net.jcip.annotations.Immutable;

/**
 * An event to notify an actor that the number of people associated with the
 * give label set has transitioned from zero to a positive number of people.
 */
@Immutable
public record PartitionCellOccupancyEvent(Object id, LabelSet labelSet) implements Event {
}
