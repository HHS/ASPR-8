package lesson.plugins.person.events;

import lesson.plugins.person.support.PersonId;
import net.jcip.annotations.Immutable;
import nucleus.Event;

@Immutable
public final class PersonAdditionEvent implements Event {

	private final PersonId personId;

	public PersonAdditionEvent(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}

}
