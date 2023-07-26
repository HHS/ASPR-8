package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;
import net.jcip.annotations.Immutable;

@Immutable
public record MaterialsProducerPropertyUpdateEvent(MaterialsProducerId materialsProducerId,
												   MaterialsProducerPropertyId materialsProducerPropertyId,
												   Object previousPropertyValue,
												   Object currentPropertyValue) implements Event {
}
