package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialId;
import net.jcip.annotations.Immutable;

/**
 * An event indicating the addition of a batch property for the given material.
 */
@Immutable
public record BatchPropertyDefinitionEvent(MaterialId materialId, BatchPropertyId batchPropertyId) implements Event {
}
