package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;

@Immutable
public record MaterialsProducerPropertyUpdateEvent(MaterialsProducerId materialsProducerId,
												   MaterialsProducerPropertyId materialsProducerPropertyId,
												   Object previousPropertyValue,
												   Object currentPropertyValue) implements Event {
}
