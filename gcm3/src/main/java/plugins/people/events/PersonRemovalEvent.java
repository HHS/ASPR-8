package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * Indicates that the given person has been removed from the simulation. All
 * references to the person are invalid.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PersonRemovalEvent implements Event {
	private final PersonId personId;

	/**
	 * Constructs the event from the give person id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li>
	 */
	public PersonRemovalEvent(PersonId personId) {
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

	/**
	 * Returns this event as a string in the form:
	 * 
	 * PersonRemovalEvent [personId="+i+"]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonRemovalEvent [personId=");
		builder.append(personId);
		builder.append("]");
		return builder.toString();
	}

}
