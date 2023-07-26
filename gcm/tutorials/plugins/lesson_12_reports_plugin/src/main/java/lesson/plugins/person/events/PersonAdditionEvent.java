package lesson.plugins.person.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import lesson.plugins.person.support.PersonId;
import net.jcip.annotations.Immutable;

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
