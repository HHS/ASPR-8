package plugins.resources.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.resources.support.ResourceId;

/**
 * An observation event indicating that a person's resource level has changed.
 *
 * @author Shawn Hatch
 */
@Immutable
public record PersonResourceUpdateEvent(PersonId personId,
										ResourceId resourceId, long previousResourceLevel,
										long currentResourceLevel) implements Event {
}
