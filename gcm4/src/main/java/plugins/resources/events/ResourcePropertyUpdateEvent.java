package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * An observation event indicating that a resource property has changed.
 *
 * @author Shawn Hatch
 */
@Immutable
public record ResourcePropertyUpdateEvent(ResourceId resourceId,
										  ResourcePropertyId resourcePropertyId,
										  Object previousPropertyValue,
										  Object currentPropertyValue) implements Event {
}
