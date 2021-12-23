package plugins.partitions.testsupport.attributes.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;

/**
 * Sets attribute value for the given person and attribute id.
 *
 */
@Immutable
public final class AttributeValueAssignmentEvent implements Event {

	private final PersonId personId;

	private final AttributeId attributeId;

	private final Object value;

	
	public AttributeValueAssignmentEvent(PersonId personId, AttributeId attributeId, Object value) {
		super();
		this.personId = personId;
		this.attributeId = attributeId;
		this.value = value;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public AttributeId getAttributeId() {
		return attributeId;
	}

	public Object getValue() {
		return value;
	}

}
