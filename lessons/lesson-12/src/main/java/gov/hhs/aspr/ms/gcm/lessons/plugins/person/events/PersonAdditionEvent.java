package gov.hhs.aspr.ms.gcm.lessons.plugins.person.events;

import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
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
