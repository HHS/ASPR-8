package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=events_alpha_change_event|code_cap=An event to notify that the alpha property has been updated. */
public record AlphaChangeEvent(int previousAlpha,int currentAlpha) implements Event {}
/* end */
