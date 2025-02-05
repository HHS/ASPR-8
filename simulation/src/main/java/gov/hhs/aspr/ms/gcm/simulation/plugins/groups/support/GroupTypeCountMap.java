package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Partition;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the mapping from the various {@link GroupTypeId} values to the
 * number of such groups a particular person is contained in. These are used to
 * match people in a {@link Partition} who are associated with some specific
 * numbers of groups of specific group types.
 */
@Immutable
public final class GroupTypeCountMap {

	private static class Data {
		private Map<GroupTypeId, Integer> map = new LinkedHashMap<>();
		private Set<GroupTypeId> groupTypeIds;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			map.putAll(data.map);
			groupTypeIds = Collections.unmodifiableSet(data.map.keySet());
			locked = data.locked;
		}

	}

	private final Data data;

	private GroupTypeCountMap(Data data) {
		this.data = data;
	}

	/**
	 * Follows the requirements of equals()
	 */
	@Override
	public int hashCode() {
		int result = 1;
		for (GroupTypeId groupTypeId : data.map.keySet()) {
			Integer value = data.map.get(groupTypeId);
			if (value.intValue() != 0) {
				result += value.hashCode();
			}
		}
		return result;
	}

	/**
	 * Returns an unmodifiable set of the {@link GroupTypeId} values contained in
	 * this {@link GroupTypeCountMap}
	 */
	public Set<GroupTypeId> getGroupTypeIds() {
		return data.groupTypeIds;
	}

	/**
	 * Two {@link GroupTypeCountMap} objects are considered equal if the POSITIVE
	 * values associated with their group type ids are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupTypeCountMap other = (GroupTypeCountMap) obj;
		for (GroupTypeId groupTypeId : data.map.keySet()) {
			int value = data.map.get(groupTypeId);
			if (value > 0) {
				int groupCount = other.getGroupCount(groupTypeId);
				if (value != groupCount) {
					return false;
				}
			}
		}
		for (GroupTypeId groupTypeId : other.data.map.keySet()) {
			int value = other.data.map.get(groupTypeId);
			if (value > 0) {
				int groupCount = getGroupCount(groupTypeId);
				if (value != groupCount) {
					return false;
				}
			}
		}
		return true;
	}


	public int getGroupCount(GroupTypeId groupTypeId) {
		Integer result = data.map.get(groupTypeId);
		if (result == null) {
			result = 0;
		}
		return result;
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Standard builder class for group type count maps. All inputs are optional.
	 */
	@NotThreadSafe
	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		public GroupTypeCountMap build() {
			if (!data.locked) {
				data.groupTypeIds = Collections.unmodifiableSet(data.map.keySet());
				validateData();
			}
			ensureImmutability();
			return new GroupTypeCountMap(data);
		}

		/**
		 * Sets the count for the given group type id
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
		 *                           groupTypeId is null</li>
		 *                           <li>{@linkplain GroupError#NEGATIVE_GROUP_COUNT}if
		 *                           the count is negative</li>
		 *                           </ul>
		 */
		public Builder setCount(GroupTypeId groupTypeId, int count) {
			ensureDataMutability();
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			if (count < 0) {
				throw new ContractException(GroupError.NEGATIVE_GROUP_COUNT);
			}
			data.map.put(groupTypeId, count);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
		}
	}

	/**
	 * Returns a standard string implementation of the form: GroupTypeCountMap
	 * [GROUP_TYPE_1=2, GROUP_TYPE_2=1] that includes only non-zero group type
	 * counts.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GroupTypeCountMap [");
		boolean first = true;
		for (GroupTypeId groupTypeId : data.map.keySet()) {
			Integer count = data.map.get(groupTypeId);
			if (count > 0) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(groupTypeId);
				sb.append("=");
				sb.append(data.map.get(groupTypeId));
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
