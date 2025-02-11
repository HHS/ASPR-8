package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;

/**
 * An event released by the global data manager whenever a global property
 * definition is added to the simulation.
 */
public record GlobalPropertyDefinitionEvent(GlobalPropertyId globalPropertyId, Object initialPropertyValue)
		implements Event {
}
