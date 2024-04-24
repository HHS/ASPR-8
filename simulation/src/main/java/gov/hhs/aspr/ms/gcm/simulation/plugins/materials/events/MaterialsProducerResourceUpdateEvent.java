package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import net.jcip.annotations.Immutable;

@Immutable
public record MaterialsProducerResourceUpdateEvent(MaterialsProducerId materialsProducerId, ResourceId resourceId,
		long previousResourceLevel, long currentResourceLevel) implements Event {
}
