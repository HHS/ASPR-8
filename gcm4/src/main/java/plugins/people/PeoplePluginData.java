package plugins.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of people containing person ids.
 * All other person initialization data is provided by other plugins.
 * 
 *
 */
@Immutable
public final class PeoplePluginData implements PluginData {
	private static class Data {
		private List<PersonId> personIds = new ArrayList<>();

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
		private boolean dataIsMutable;

		private void ensureDataMutability() {
			if (!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

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
			ensureDataMutability();
			validatePersonIdIsValid(personId);
			validatePersonDoesNotExist(data, personId);
			int personIndex = personId.getValue();
			while (personIndex >= data.personIds.size()) {
				data.personIds.add(null);
			}
			data.personIds.set(personIndex, personId);
			return this;
		}
	}

	private PeoplePluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the list person ids such that each PersonId is located at the
	 * index associated with the person id's value. Thus, this list may contain
	 * null entries.
	 */
	public List<PersonId> getPersonIds() {
		return Collections.unmodifiableList(data.personIds);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	/*
	 * precondition: person id is not null 
	 */
	private static void validatePersonDoesNotExist(final Data data, final PersonId personId) {
		int personIndex = personId.getValue();
		if (personIndex >= data.personIds.size()) {
			return;
		}
		if (data.personIds.get(personIndex) != null) {
			throw new ContractException(PersonError.DUPLICATE_PERSON_ID, personId);
		}
	}

	private static void validatePersonIdIsValid(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}		
	}
}
