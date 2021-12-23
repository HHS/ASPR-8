package plugins.people.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonError;
import util.ContractException;

/**
 * Creates a new person located in the given region and compartment.
 *
 */
@Immutable
public final class BulkPersonCreationEvent implements Event {

	private final BulkPersonContructionData bulkPersonContructionData;

	/**
	 * Constructs this event
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_BULK_PERSON_CONTRUCTION_DATA}
	 *             if the bulk person construction data is null</li>
	 */
	public BulkPersonCreationEvent(BulkPersonContructionData bulkPersonContructionData) {
		if (bulkPersonContructionData == null) {
			throw new ContractException(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA);
		}
		this.bulkPersonContructionData = bulkPersonContructionData;
	}

	/**
	 * Returns the bulk person construction data used to create this event
	 */
	public BulkPersonContructionData getBulkPersonContructionData() {
		return bulkPersonContructionData;
	}

}
