package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.groups.support.GroupTypeId;

/**
 * An event indicating that a group type has been created
 *
 * @author Shawn Hatch
 */
@Immutable
public record GroupTypeAdditionEvent(GroupTypeId groupTypeId) implements Event {
}
