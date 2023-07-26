package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.resources.support.ResourceId;

@Immutable
public record MaterialsProducerResourceUpdateEvent(MaterialsProducerId materialsProducerId,
												   ResourceId resourceId,
												   long previousResourceLevel,
												   long currentResourceLevel) implements Event {
}