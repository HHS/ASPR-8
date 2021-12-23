package plugins.people.initialdata;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.DataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * An immutable container of the initial state of people. It contains: <BR>
 * <ul>
 * <li>person ids</li>
 * </ul>
 * 
 * All other person initialization data is provided by other plugins.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PeopleInitialData implements DataView {
	private static class Data {
		private Set<PersonId> personIds = new LinkedHashSet<>();
	}

	private final Data data;

	/**
	 * Returns a builder instance for this class
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for PeopleInitialData
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Returns the PeopleInitialData resulting from the person ids collected
		 * by this builder.
		 */
		public PeopleInitialData build() {
			try {
				return new PeopleInitialData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a person.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 *             <li>{@linkplain PersonError#DUPLICATE_PERSON_ID} if the
		 *             person id is already contained</li>
		 * 
		 */
		public Builder addPersonId(PersonId personId) {
			validatePersonIdNotNull(personId);
			validatePersonDoesNotExist(data, personId);
			data.personIds.add(personId);
			return this;
		}
	}

	private static void validatePersonDoesNotExist(final Data data, final PersonId personId) {
		if (data.personIds.contains(personId)) {
			throw new ContractException(PersonError.DUPLICATE_PERSON_ID, personId);
		}
	}

	private static void validatePersonIdNotNull(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private PeopleInitialData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the set of person ids 
	 */
	public Set<PersonId> getPersonIds() {
		return new LinkedHashSet<>(data.personIds);
	}

}
