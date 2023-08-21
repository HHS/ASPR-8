package gov.hhs.aspr.ms.gcm.plugins.people.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Indicates that the given person has been removed from the simulation. All
 * references to the person are invalid.
 */
@Immutable
public record PersonRemovalEvent(PersonId personId) implements Event {
	/**
	 * Constructs the event from the give person id
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID}</li>
	 *                           </ul>
	 */
	public PersonRemovalEvent {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
}
