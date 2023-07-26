package plugins.partitions.testsupport.attributes.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;

@Immutable
public record AttributeUpdateEvent(PersonId personId,
								   AttributeId attributeId,
								   Object previousValue, Object currentValue) implements Event {
	
	@SuppressWarnings("unchecked")
	public <T> T getPreviousValue() {
		return(T)previousValue;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCurrentValue() {
		return(T)currentValue;
	}

}
