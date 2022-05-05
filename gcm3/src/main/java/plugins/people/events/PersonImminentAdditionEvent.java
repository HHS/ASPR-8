package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

@Immutable
public final class PersonImminentAdditionEvent implements Event {
	private final PersonId personId;
	private final PersonConstructionData personConstructionData;

	/**
	 * Constructs the event from the given person id and person construction
	 * data
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 */
	public PersonImminentAdditionEvent(final PersonId personId, PersonConstructionData personConstructionData) {

		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (personConstructionData == null) {
			throw new ContractException(PersonError.NULL_PERSON_CONSTRUCTION_DATA);
		}
		this.personId = personId;
		this.personConstructionData = personConstructionData;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the person construction data used to create this event
	 */
	public PersonConstructionData getPersonConstructionData() {
		return personConstructionData;
	}

	
}
