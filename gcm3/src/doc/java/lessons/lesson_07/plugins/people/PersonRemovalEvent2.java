package lessons.lesson_07.plugins.people;

import net.jcip.annotations.Immutable;
import nucleus.Event;

@Immutable
public final class PersonRemovalEvent2 implements Event {

	private final PersonId personId;

	public PersonRemovalEvent2(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}

}
