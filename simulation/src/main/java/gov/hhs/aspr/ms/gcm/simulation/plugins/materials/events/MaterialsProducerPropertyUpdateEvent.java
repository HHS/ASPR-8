package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerPropertyId;
import net.jcip.annotations.Immutable;

@Immutable
public record MaterialsProducerPropertyUpdateEvent(MaterialsProducerId materialsProducerId,
		MaterialsProducerPropertyId materialsProducerPropertyId, Object previousPropertyValue,
		Object currentPropertyValue) implements Event {
}
