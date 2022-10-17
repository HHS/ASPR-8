package plugins.partitions.testsupport.attributes.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;

@Immutable
public class AttributeUpdateEvent implements Event {
	private final PersonId personId;
	private final AttributeId attributeId;
	private final Object previousValue;
	private final Object currentValue;

	public AttributeUpdateEvent(final PersonId personId, final AttributeId attributeId, final Object previousValue, final Object currentValue) {
		super();
		this.personId = personId;
		this.attributeId = attributeId;
		this.previousValue = previousValue;
		this.currentValue = currentValue;
	}

	public Object getCurrentValue() {
		return currentValue;
	}

	public AttributeId getAttributeId() {
		return attributeId;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public Object getPreviousValue() {
		return previousValue;
	}
	
}
