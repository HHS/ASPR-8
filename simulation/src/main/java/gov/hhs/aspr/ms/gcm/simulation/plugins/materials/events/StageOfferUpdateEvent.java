package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;
import net.jcip.annotations.Immutable;

@Immutable
public record StageOfferUpdateEvent(StageId stageId, boolean previousOfferState, boolean currentOfferState)
		implements Event {
}
