package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerPropertyId;
import net.jcip.annotations.Immutable;

/**
 * An event released by the materials data manager whenever a materials producer
 * property definition is added to the simulation.
 */
@Immutable
public record MaterialsProducerPropertyDefinitionEvent(MaterialsProducerPropertyId materialsProducerPropertyId)
		implements Event {
}
