package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * Indicates that the given person will be removed from the simulation
 * imminently, but all references to the person will still function at the time
 * this event is received. No further events or plans should be generated that
 * reference the person.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PersonImminentRemovalObservationEvent implements Event {
	private final PersonId personId;

	/**
	 * Constructs the event from the give person id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li>
	 */
	public PersonImminentRemovalObservationEvent(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		this.personId = personId;

	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns this event as a string in the form:
	 * 
	 * PersonRemovalObservationEvent [personId="+i+"]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonRemovalObservationEvent [personId=");
		builder.append(personId);
		builder.append("]");
		return builder.toString();
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<PersonImminentRemovalObservationEvent> EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(PersonImminentRemovalObservationEvent.class, LabelerId.ALL,
			PersonImminentRemovalObservationEvent.class);

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonImminentRemovalObservationEvent} events. Matches all such
	 * events.
	 */
	public static EventLabel<PersonImminentRemovalObservationEvent> getEventLabel() {
		return EVENT_LABEL_INSTANCE;
	}
	/**
	 * Returns an event labeler for {@link PersonImminentRemovalObservationEvent}
	 */
	public static EventLabeler<PersonImminentRemovalObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.ALL, PersonImminentRemovalObservationEvent.class, (context, event) -> EVENT_LABEL_INSTANCE);
	}
}
