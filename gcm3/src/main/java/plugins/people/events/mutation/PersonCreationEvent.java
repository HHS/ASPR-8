package plugins.people.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonContructionData;

/**
 * Creates a new person located in the given region and compartment.
 *
 */
@Immutable
public final class PersonCreationEvent implements Event {

	private final PersonContructionData personContructionData;

	/**
	 * Constructs the event from the given person construction data
	 */
	public PersonCreationEvent(PersonContructionData personContructionData) {
		this.personContructionData = personContructionData;
	}

	/**
	 * Returns the person construction data used to create this event
	 */
	public PersonContructionData getPersonContructionData() {
		return personContructionData;
	}

}
