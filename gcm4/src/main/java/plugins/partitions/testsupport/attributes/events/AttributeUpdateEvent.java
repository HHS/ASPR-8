package plugins.partitions.testsupport.attributes.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;

@Immutable
public record AttributeUpdateEvent(PersonId personId,
								   AttributeId attributeId,
								   Object previousValue, Object currentValue) implements Event {
}
