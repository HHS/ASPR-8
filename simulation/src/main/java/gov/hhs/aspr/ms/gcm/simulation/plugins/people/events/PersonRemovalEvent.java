package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * Indicates that the given person has been removed from the simulation. All
 * references to the person are invalid.
 */
@Immutable
public record PersonRemovalEvent(PersonId personId) implements Event {
	/**
	 * Constructs the event from the give person id
	 *
	 * @throws ContractException {@linkplain PersonError#NULL_PERSON_ID}
	 */
	public PersonRemovalEvent {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
}
