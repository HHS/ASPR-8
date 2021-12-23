package plugins.people.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

@Immutable
public class PersonCreationObservationEvent implements Event {
	private final PersonId personId;
	private final PersonContructionData personContructionData;

	/**
	 * Constructs the event from the given person id and person construction
	 * data
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_CONTRUCTION_DATA} if
	 *             the person construction data is null</li>
	 */
	public PersonCreationObservationEvent(final PersonId personId, PersonContructionData personContructionData) {

		if(personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if(personContructionData == null) {
			throw new ContractException(PersonError.NULL_PERSON_CONTRUCTION_DATA);
		}
		this.personId = personId;
		this.personContructionData = personContructionData;
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
	public PersonContructionData getPersonContructionData() {
		return personContructionData;
	}
	
	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonCreationObservationEvent} events. Matches all such
	 * events.
	 */
	public static EventLabel<PersonCreationObservationEvent> getEventLabel() {
		return EVENT_LABEL_INSTANCE;
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	
	private final static EventLabel<PersonCreationObservationEvent> EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(PersonCreationObservationEvent.class, LabelerId.ALL,
			PersonCreationObservationEvent.class);
	/**
	 * Returns an event labeler for {@link PersonCreationObservationEvent}
	 */
	public static EventLabeler<PersonCreationObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.ALL, PersonCreationObservationEvent.class, (context, event) -> EVENT_LABEL_INSTANCE);
	}
}
