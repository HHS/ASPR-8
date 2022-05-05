package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * An event to notify the plugins that a person has been fully added to the
 * simulation.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public final class PersonAdditionEvent implements Event {
	private final PersonId personId;

	/**
	 * Constructs the event from the given person id and person construction
	 * data
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 */
	public PersonAdditionEvent(final PersonId personId) {

		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		this.personId = personId;

	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

}
