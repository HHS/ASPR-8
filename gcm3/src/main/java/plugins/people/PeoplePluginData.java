package plugins.people;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.util.ContractException;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * An immutable container of the initial state of people containing person ids.
 * All other person initialization data is provided by other plugins.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PeoplePluginData implements PluginData {
	private static class Data {
		private Set<PersonId> personIds = new LinkedHashSet<>();
		

		public Data() {
		}

		public Data(Data data) {
			this.personIds.addAll(data.personIds);
		}
	}

	private final Data data;

	/**
	 * Returns a new builder instance for this class
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for PeoplePluginData
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;

		}

		/**
		 * Returns the PeopleInitialData resulting from the person ids collected
		 * by this builder.
		 */
		public PeoplePluginData build() {
			try {
				return new PeoplePluginData(data);
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
	

	private PeoplePluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the set of person ids stored in this container
	 */
	public Set<PersonId> getPersonIds() {
		return new LinkedHashSet<>(data.personIds);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
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
}
