package gov.hhs.aspr.ms.gcm.plugins.people.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event to notify the plugins that a person has been fully added to the
 * simulation.
 */

@Immutable
public record PersonAdditionEvent(PersonId personId) implements Event {
	/**
	 * Constructs the event from the given person id and person construction data
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null</li>
	 *                           </ul>
	 */
	public PersonAdditionEvent {

		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
}
