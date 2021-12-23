package plugins.personproperties.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;

/**
 * Sets property value for the given person and property.
 *
 */
@Immutable
public final class PersonPropertyValueAssignmentEvent implements Event {

	private final PersonId personId;

	private final PersonPropertyId personPropertyId;

	private final Object personPropertyValue;

	
	public PersonPropertyValueAssignmentEvent(PersonId personId, PersonPropertyId personPropertyId, Object personPropertyValue) {
		super();
		this.personId = personId;
		this.personPropertyId = personPropertyId;
		this.personPropertyValue = personPropertyValue;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	public Object getPersonPropertyValue() {
		return personPropertyValue;
	}

}
