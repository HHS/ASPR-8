package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

@Immutable
public final class PersonAdditionEvent implements Event {
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
	public PersonAdditionEvent(final PersonId personId, PersonConstructionData personConstructionData) {

		if(personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if(personConstructionData == null) {
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
	
	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonAdditionEvent} events. Matches all such
	 * events.
	 */
	public static EventLabel<PersonAdditionEvent> getEventLabel() {
		return EVENT_LABEL_INSTANCE;
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	
	private final static EventLabel<PersonAdditionEvent> EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(PersonAdditionEvent.class, LabelerId.ALL,
			PersonAdditionEvent.class);
	/**
	 * Returns an event labeler for {@link PersonAdditionEvent}
	 */
	public static EventLabeler<PersonAdditionEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.ALL, PersonAdditionEvent.class, (context, event) -> EVENT_LABEL_INSTANCE);
	}
}
