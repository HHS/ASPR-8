package plugins.people.events;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * An event for notifying plugins of the completed construction of multiple
 * people.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class BulkPersonAdditionEvent implements Event {

	private static class Data {
		private final Set<PersonId> people = new LinkedHashSet<>();
	}

	private final Data data;

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		private Builder() {}
		
		private Data data = new Data();

		/**
		 * Returns the BulkPersonAdditionEvent from the collect person id values
		 */
		public BulkPersonAdditionEvent build() {
			try {
				return new BulkPersonAdditionEvent(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a person to the event
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PersonError#DUPLICATE_PERSON_ID} if the person
		 *             id was already added</li>
		 */
		public Builder addPersonId(PersonId personId) {
			if (personId == null) {
				throw new ContractException(PersonError.NULL_PERSON_ID);
			}

			boolean added = data.people.add(personId);
			if (!added) {
				throw new ContractException(PersonError.DUPLICATE_PERSON_ID);
			}
			return this;
		}
	}

	/**
	 * Constructs the event from the given person and bulk person construction
	 * data. The person id will correspond to the first person created from the
	 * BulkPersonConstructionData.
	 * 
	 */
	private BulkPersonAdditionEvent(Data data) {
		this.data = data;
	}

	/**
	 * Returns the person id for the first person that was created from the
	 * BulkPersonConstructionData. People are constructed contiguously in the
	 * order contained in the BulkPersonConstructionData.
	 */
	public List<PersonId> getPeople() {
		return new ArrayList<>(data.people);
	}

}
