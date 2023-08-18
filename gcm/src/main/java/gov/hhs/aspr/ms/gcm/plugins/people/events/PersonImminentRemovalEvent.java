package gov.hhs.aspr.ms.gcm.plugins.people.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Indicates that the given person will be removed from the simulation
 * imminently, but all references to the person will still function at the time
 * this event is received. No further events or plans should be generated that
 * reference the person.
 */
@Immutable
public record PersonImminentRemovalEvent(PersonId personId) implements Event {
	/**
	 * Constructs the event from the give person id
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PersonError#NULL_PERSON_ID}</li>
	 *                           </ul>
	 */
	public PersonImminentRemovalEvent {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
}
