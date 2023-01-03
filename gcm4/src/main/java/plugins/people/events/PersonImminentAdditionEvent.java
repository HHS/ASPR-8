package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * An event for notifying plugins that a person is being added to the
 * simulation. During this process, the person is not fully formed until all
 * subscribers to the event have been invoked.
 *
 */
@Immutable
public record PersonImminentAdditionEvent(PersonId personId,
										  PersonConstructionData personConstructionData) implements Event {
	/**
	 * Constructs the event from the given person id and person construction
	 * data
	 *
	 * @throws ContractException <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *                           is null</li>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA} if
	 *                           the person construction data is null</li>
	 */
	public PersonImminentAdditionEvent {

		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (personConstructionData == null) {
			throw new ContractException(PersonError.NULL_PERSON_CONSTRUCTION_DATA);
		}
	}
}
