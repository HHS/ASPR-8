package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.EventLabel;
import nucleus.util.ContractException;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * An event for observing the construction multiple people from a
 * {@linkplain BulkPersonCreationEvent} event.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class BulkPersonAdditionEvent implements Event {
	private final PersonId personId;
	private final BulkPersonConstructionData bulkPersonConstructionData;

	/**
	 * Constructs the event from the given person and bulk person construction
	 * data. The person id will correspond to the first person created from the
	 * BulkPersonConstructionData.
	 * 
	 */
	public BulkPersonAdditionEvent(final PersonId personId, BulkPersonConstructionData bulkPersonConstructionData) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (bulkPersonConstructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONSTRUCTION_DATA);
		}

		this.personId = personId;
		this.bulkPersonConstructionData = bulkPersonConstructionData;
	}

	/**
	 * Returns the person id for the first person that was created from the
	 * BulkPersonConstructionData. People are constructed contiguously in the
	 * order contained in the BulkPersonConstructionData.
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the BulkPersonConstructionData used to create this event.
	 */
	public BulkPersonConstructionData getBulkPersonConstructionData() {
		return bulkPersonConstructionData;
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link BulkPersonAdditionEvent} events. Matches all such events.
	 */
	public static EventLabel<BulkPersonAdditionEvent> getEventLabel() {
		return EVENT_LABEL_ALL;
	}

	private static enum LabelerId implements EventLabelerId {
		ALL
	}

	private final static EventLabel<BulkPersonAdditionEvent> EVENT_LABEL_ALL = new EventLabel<>(BulkPersonAdditionEvent.class, LabelerId.ALL, BulkPersonAdditionEvent.class);

	/**
	 * Returns an event labeler for {@link BulkPersonAdditionEvent}
	 */
	public static EventLabeler<BulkPersonAdditionEvent> getEventLabeler() {
		return EventLabeler	.builder(BulkPersonAdditionEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> EVENT_LABEL_ALL)//
							.build();
	}
}
