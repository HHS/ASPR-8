package plugins.people.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

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

}
