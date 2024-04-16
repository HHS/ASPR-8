package gov.hhs.aspr.ms.gcm.simulation.plugins.people.events;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An event to notify the plugins that a person has been fully added to the
 * simulation.
 */
@Immutable
public record PersonAdditionEvent(PersonId personId) implements Event {
	/**
	 * Constructs the event from the given person id and person construction data
	 *
	 * @throws ContractException {@linkplain PersonError#NULL_PERSON_ID} if the
	 *                           person id is null
	 */
	public PersonAdditionEvent {

		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}
}
