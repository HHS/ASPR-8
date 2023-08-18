package gov.hhs.aspr.ms.gcm.plugins.people.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event for notifying plugins that a person is being added to the
 * simulation. During this process, the person is not fully formed until all
 * subscribers to the event have been invoked.
 */
@Immutable
public record PersonImminentAdditionEvent(PersonId personId, PersonConstructionData personConstructionData)
		implements Event {
	/**
	 * Constructs the event from the given person id and person construction data
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           <li>{@linkplain PersonError#NULL_PERSON_CONSTRUCTION_DATA}
	 *                           if the person construction data is null</li>
	 *                           </ul>
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
