package plugins.people.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * An event for observing the construction multiple people from a
 * {@linkplain BulkPersonCreationEvent} event.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class BulkPersonCreationObservationEvent implements Event {
	private final PersonId personId;
	private final BulkPersonContructionData bulkPersonContructionData;

	/**
	 * Constructs the event from the given person and bulk person construction
	 * data. The person id will correspond to the first person created from the
	 * BulkPersonContructionData.
	 * 
	 */
	public BulkPersonCreationObservationEvent(final PersonId personId, BulkPersonContructionData bulkPersonContructionData) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (bulkPersonContructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA);
		}

		this.personId = personId;
		this.bulkPersonContructionData = bulkPersonContructionData;
	}

	/**
	 * Returns the person id for the first person that was created from the
	 * BulkPersonContructionData. People are constructed contiguously in the
	 * order contained in the BulkPersonContructionData.
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the BulkPersonContructionData used to create this event.
	 */
	public BulkPersonContructionData getBulkPersonContructionData() {
		return bulkPersonContructionData;
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link BulkPersonCreationObservationEvent} events. Matches all such
	 * events.
	 */
	public static EventLabel<BulkPersonCreationObservationEvent> getEventLabel() {
		return EVENT_LABEL_INSTANCE;
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BulkPersonCreationObservationEvent> EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(BulkPersonCreationObservationEvent.class, LabelerId.ALL,
			BulkPersonCreationObservationEvent.class);

	/**
	 * Returns an event labeler for {@link BulkPersonCreationObservationEvent}
	 */
	public static EventLabeler<BulkPersonCreationObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(
				LabelerId.ALL,
				BulkPersonCreationObservationEvent.class,
				(context, event) -> EVENT_LABEL_INSTANCE);
	}
}
