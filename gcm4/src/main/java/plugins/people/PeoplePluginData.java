package plugins.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
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
		private int personCount = -1;
		private List<PersonRange> personRanges = new ArrayList<>();
		private List<PersonId> personIds;
		private boolean locked;

		public Data() {
		}

		public Data(Data data) {
			this.personCount = data.personCount;
			this.personRanges.addAll(data.personRanges);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + personCount;
			result = prime * result + ((personRanges == null) ? 0 : personRanges.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (personCount != other.personCount) {
				return false;
			}
			if (personRanges == null) {
				if (other.personRanges != null) {
					return false;
				}
			} else if (!personRanges.equals(other.personRanges)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [personCount=");
			builder.append(personCount);
			builder.append(", personRanges=");
			builder.append(personRanges);
			builder.append(", personIds=");
			builder.append(personIds);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
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

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
			}
		}

		private void validate() {
			if (data.personCount >= 0) {
				for (PersonRange personRange : data.personRanges) {
					if (personRange.getHighPersonId() >= data.personCount) {
						throw new ContractException(PersonError.INVALID_PERSON_COUNT);
					}
				}
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {

				/*
				 * Copy the person ranges and sort them by their low values.
				 * 
				 * These entries can overlap, so we will rebuild them so that
				 * all overlaps are gone
				 * 
				 */
				List<PersonRange> list = new ArrayList<>(data.personRanges);
				Collections.sort(list);

				// create a list of person ranges that will hold the
				// non-overlapping entries
				List<PersonRange> list2 = new ArrayList<>();

				// a and b are the low and high values of a person range that
				// has yet to be added
				int a = -1;
				int b = -1;

				/*
				 * low and high are the values from the current person range
				 */
				int low;
				int high;

				/*
				 * Count is the number of person id values we will be recording
				 * It will be used to set the size of the person ids array list
				 * so that it won't have wasted allocations
				 */
				int count = 0;
				for (PersonRange personRange : list) {
					low = personRange.getLowPersonId();
					high = personRange.getHighPersonId();
					if (a < 0) {
						a = low;
						b = high;
					} else {
						if (low > b + 1) {
							count += (b - a + 1);
							list2.add(new PersonRange(a, b));
							a = low;
							b = high;
						} else {
							if (high > b) {
								b = high;
							}
						}
					}
				}

				/*
				 * The last values of a and b may not have been converted onto
				 * the second list
				 */
				if (a >= 0) {
					count += (b - a + 1);
					list2.add(new PersonRange(a, b));
				}

				data.personRanges = list2;
				data.personIds = new ArrayList<>(count);

				if (data.personCount < 0) {
					data.personCount = 0;
					if (!data.personRanges.isEmpty()) {
						data.personCount = data.personRanges.get(data.personRanges.size() - 1).getHighPersonId() + 1;
					}
				}

				// We now transfer the non-overlapping person ranges
				for (PersonRange personRange : data.personRanges) {
					for (int id = personRange.getLowPersonId(); id <= personRange.getHighPersonId(); id++) {
						data.personIds.add(new PersonId(id));
					}
				}

				// Finally, we mark the data as locked
				data.locked = true;
			}
		}

		private Builder(Data data) {
			this.data = data;

		}

		/**
		 * Returns the PeopleInitialData resulting from the person ids collected
		 * by this builder.
		 * 
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#INVALID_PERSON_COUNT} if the
		 *             person count does not exceed all person range values</li>
		 * 
		 */
		public PeoplePluginData build() {
			try {
				validate();
				ensureImmutability();
				return new PeoplePluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a person range. Overlapping person ranges are tolerated.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person
		 *             id is null</li>
		 * 
		 */
		public Builder addPersonRange(PersonRange personRange) {
			ensureDataMutability();
			validatePersonRandgeIsValid(personRange);
			data.personRanges.add(personRange);
			return this;
		}

		/**
		 * Sets the person count. Defaults to one more than the maximum person
		 * id of any of the person ranges added. If no person ranges are added,
		 * the default is zero.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#NEGATIVE_PERSON_COUNT} if the
		 *             person count is negative</li>
		 */
		public Builder setPersonCount(int personCount) {
			ensureDataMutability();
			validatePersonCount(personCount);
			data.personCount = personCount;
			return this;
		}
	}

	private PeoplePluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns an unmodifiable, ordered list person ids contained by this people
	 * plugin data.
	 */
	public List<PersonId> getPersonIds() {
		return Collections.unmodifiableList(data.personIds);
	}

	/**
	 * Returns an unmodifiable, ordered, non-overlapping, list person ranges
	 * contained by this people plugin data.
	 */
	public List<PersonRange> getPersonRanges() {
		return Collections.unmodifiableList(data.personRanges);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	private static void validatePersonRandgeIsValid(PersonRange personRange) {
		if (personRange == null) {
			throw new ContractException(PersonError.NULL_PERSON_RANGE);
		}
	}

	private static void validatePersonCount(int personCount) {
		if (personCount < 0) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_COUNT);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PeoplePluginData))
			return false;
		PeoplePluginData that = (PeoplePluginData) o;
		return data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	public int getPersonCount() {
		return data.personCount;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PeoplePluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}
