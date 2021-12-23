package plugins.people.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonId;

/**
 * Requests that the person be removed from the simulation.
 *
 *
 */
@Immutable
public final class PersonRemovalRequestEvent implements Event {

	private final PersonId personId;

	/**
	 * Constructs this event from the given person id
	 *
	 * 
	 */
	public PersonRemovalRequestEvent(PersonId personId) {
		this.personId = personId;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

}
