package gov.hhs.aspr.ms.gcm.lessons.plugins.people;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=plugin_dependencies_person_removal_event|code_cap=An event signifying that a person has been removed from the simulation.*/
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
