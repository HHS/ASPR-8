package gov.hhs.aspr.ms.gcm.plugins.globalproperties.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import net.jcip.annotations.Immutable;

/**
 * An event released by the global data manager whenever a global property is
 * changed.
 */
@Immutable
public record GlobalPropertyUpdateEvent(GlobalPropertyId globalPropertyId, Object previousPropertyValue,
		Object currentPropertyValue) implements Event {
}
