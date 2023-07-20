package lesson.plugins.people;

import net.jcip.annotations.Immutable;
import nucleus.Event;

@Immutable
/* start code_ref=plugin_dependencies_person_removal_event */
public final class PersonRemovalEvent implements Event {

	private final PersonId personId;

	public PersonRemovalEvent(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}

}
/* end */
