package gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An immutable container of the initial state of people containing person ids.
 * All other person initialization data is provided by other plugins.
 */
@Immutable
public final class PeoplePluginData implements PluginData {

	/*
	 * The person ids are calculated when the data is being locked and are retained
	 * here instead of in the PeoplePluginData instance for efficiency. We do not
	 * copy the person ids in the copy constructor of the data class since we will
	 * be setting the locked to false and will just have to recalculate the person
	 * ids later.
	 */
	private static class Data {
		private int personCount = -1;
		private List<PersonRange> personRanges = new ArrayList<>();
		private List<PersonId> personIds;
		private double assignmentTime;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			locked = data.locked;
			this.personCount = data.personCount;
			this.personRanges.addAll(data.personRanges);
			this.assignmentTime = data.assignmentTime;
		}

		@Override
		public int hashCode() {
			/*
			 * See notes in equals()
			 */
			final int prime = 31;
			int result = 1;
			long temp = Double.doubleToLongBits(assignmentTime);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + personCount;
			result = prime * result + personRanges.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			/*
			 * This boilerplate implementation works since the person ranges are sorted and
			 * joined during the build process, leaving the person ranges unambiguously
			 * ordered.
			 */
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (Double.doubleToLongBits(assignmentTime) != Double.doubleToLongBits(other.assignmentTime)) {
				return false;
			}
			if (personCount != other.personCount) {
				return false;
			}
			if (!personRanges.equals(other.personRanges)) {
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
			builder.append(", assignmentTime=");
			builder.append(assignmentTime);
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
			} else {
				for (PersonRange personRange : data.personRanges) {
					if (personRange.getHighPersonId() >= data.personCount) {
						data.personCount = personRange.getHighPersonId();
					}
				}
				data.personCount++;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {

				/*
				 * Copy the person ranges and sort them by their low values.
				 * 
				 * These entries can overlap, so we will rebuild them so that all overlaps are
				 * gone
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
				 * Count is the number of person id values we will be recording It will be used
				 * to set the size of the person ids array list so that it won't have wasted
				 * allocations
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
				 * The last values of a and b may not have been converted onto the second list
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
		 * Returns the PeopleInitialData resulting from the person ids collected by this
		 * builder.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PersonError#INVALID_PERSON_COUNT}
		 *                           if the person count does not exceed all person
		 *                           range values</li>
		 *                           </ul>
		 */
		public PeoplePluginData build() {
			validate();
			ensureImmutability();
			return new PeoplePluginData(data);
		}

		/**
		 * Adds a person range. Overlapping person ranges are tolerated.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null</li>
		 *                           </ul>
		 */
		public Builder addPersonRange(PersonRange personRange) {
			ensureDataMutability();
			validatePersonRangeIsValid(personRange);
			data.personRanges.add(personRange);
			return this;
		}

		/**
		 * Sets the person count. Defaults to one more than the maximum person id of any
		 * of the person ranges added. If no person ranges are added, the default is
		 * zero. This reflects the number of person id values that have been issued.
		 * Note that this is not the same as the number of people and will be greater
		 * than the highest id value of any existing person.
		 */
		public Builder setPersonCount(int personCount) {
			ensureDataMutability();
			validatePersonCount(personCount);
			data.personCount = personCount;
			return this;
		}

		/**
		 * Resets the person count
		 */
		public Builder resetPersonCount() {
			ensureDataMutability();
			data.personCount = -1;
			return this;
		}

		/**
		 * Sets the time for the last person added to the population. Defaults to zero.
		 */
		public Builder setAssignmentTime(double assignmentTime) {
			ensureDataMutability();
			data.assignmentTime = assignmentTime;
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
	public Builder toBuilder() {
		return new Builder(data);
	}

	private static void validatePersonRangeIsValid(PersonRange personRange) {
		if (personRange == null) {
			throw new ContractException(PersonError.NULL_PERSON_RANGE);
		}
	}

	private static void validatePersonCount(int personCount) {
		if (personCount < 0) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_COUNT);
		}
	}

	/**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PeoplePluginData)) {
			return false;
		}
		PeoplePluginData other = (PeoplePluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the number of person id values that have been issued. Note that this
	 * is not the same as the number of people and will be greater than the highest
	 * id value of any existing person.
	 */
	public int getPersonCount() {
		return data.personCount;
	}

	public double getAssignmentTime() {
		return data.assignmentTime;
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
