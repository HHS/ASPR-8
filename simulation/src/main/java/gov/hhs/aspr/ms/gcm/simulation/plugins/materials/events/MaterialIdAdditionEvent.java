package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialId;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a material type has been added
 */
@Immutable
public record MaterialIdAdditionEvent(MaterialId materialId) implements Event {
}
