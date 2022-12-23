package plugins.globalproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.globalproperties.support.GlobalPropertyId;

/**
 * An event released by the global data manager whenever a global property is
 * changed.
 *
 * @author Shawn Hatch
 */

@Immutable
public record GlobalPropertyUpdateEvent(GlobalPropertyId globalPropertyId,
										Object previousPropertyValue,
										Object currentPropertyValue) implements Event {
}
