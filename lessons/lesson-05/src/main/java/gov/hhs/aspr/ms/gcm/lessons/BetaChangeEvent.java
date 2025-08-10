package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=events_beta_change_event|code_cap=An event to notify that the beta property has been updated.*/
public record BetaChangeEvent(double previousBeta,double currentBeta) implements Event {
}
/* end */
