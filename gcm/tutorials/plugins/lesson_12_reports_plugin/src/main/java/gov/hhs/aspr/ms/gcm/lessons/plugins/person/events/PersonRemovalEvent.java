package lesson.plugins.person.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import lesson.plugins.person.support.PersonId;
import net.jcip.annotations.Immutable;

@Immutable
public final class PersonRemovalEvent implements Event {

	private final PersonId personId;

	public PersonRemovalEvent(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}

}
