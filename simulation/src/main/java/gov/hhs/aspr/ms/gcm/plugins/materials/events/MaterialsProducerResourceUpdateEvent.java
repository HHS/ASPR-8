package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;

@Immutable
public record MaterialsProducerResourceUpdateEvent(MaterialsProducerId materialsProducerId, ResourceId resourceId,
		long previousResourceLevel, long currentResourceLevel) implements Event {
}
