package plugins.groups.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import plugins.people.support.PersonError;
import util.errors.ContractException;

public class BulkGroupMembershipData {

	private static class Data {

		/*
		 * Map from int(group id) -> Group type id
		 */
		private List<GroupTypeId> groupTypes = new ArrayList<>();

		/*
		 * Integer(PersonId)->List(GroupId)
		 */
		private Map<Integer, List<Integer>> groupMemberships = new LinkedHashMap<>();

		private int maxPersonIndex = -1;

		

		/*
		 * An empty list of Group id values used as the groups for a person when
		 * that person was not added to the group memberships.
		 */
		private final List<Integer> emptyGroupIndicesList = Collections.unmodifiableList(new ArrayList<>());

		private final Set<GroupPropertyId> emptyGroupPropertySet = Collections.unmodifiableSet(new LinkedHashSet<>());

		private Map<Integer, Map<GroupPropertyId, Object>> groupPropertyValues = new LinkedHashMap<>();
	}

	private final Data data;

	private BulkGroupMembershipData(Data data) {
		this.data = data;
	}

	/**
	 * Returns a builder for {@link BulkGroupMembershipData}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for {@linkplain BulkGroupMembershipData}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Builder() {

		}

		private Data data = new Data();

		/**
		 * Builds the {@link BulkGroupMembershipData} from the collected data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if a group
		 *             membership was a negative group index</li>
		 * 
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if a group
		 *             membership was added for a group index that was not added
		 *             as a group</li>
		 */
		public BulkGroupMembershipData build() {
			try {
				validate();
				return new BulkGroupMembershipData(data);
			} finally {
				data = new Data();
			}
		}

		private void validate() {
			// show that the group indexes for each person are in bounds.
			// Negative indexes are rejected when entered.
			int maxGroupIndex = data.groupTypes.size();
			for (Integer personIndex : data.groupMemberships.keySet()) {
				List<Integer> groupIndices = data.groupMemberships.get(personIndex);
				for (Integer groupIndex : groupIndices) {
					if (groupIndex >= maxGroupIndex) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
					}
				}
			}

			// show that the collected group property values are associated with
			// valid group id values
			for (Integer groupIndex : data.groupPropertyValues.keySet()) {
				if (groupIndex >= maxGroupIndex) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
				}
			}

		}

		/**
		 * Add a group with the given group type id.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if a group
		 *             type id is null</li>
		 * 
		 * 
		 */
		public Builder addGroup(GroupTypeId groupTypeId) {
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			data.groupTypes.add(groupTypeId);
			return this;
		}

		/**
		 * Set a group property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group
		 *             index is negative</li>
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
		 *             group property id is null</li>
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_VALUE} if
		 *             the property value is null</li>
		 * 
		 * 
		 */
		public Builder setGroupPropertyValue(int groupIndex, GroupPropertyId groupPropertyId, Object propertyValue) {
			if (groupIndex < 0) {
				throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
			}
			if (groupPropertyId == null) {
				throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
			}
			if (propertyValue == null) {
				throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
			}
			Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupIndex);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.groupPropertyValues.put(groupIndex, map);
			}
			map.put(groupPropertyId, propertyValue);
			return this;
		}

		/**
		 * Add a person to a group
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the
		 *             person index is negative</li>
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group
		 *             index is negative</li>
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_MEMBERSHIP} if
		 *             the person index is already associated with the group
		 *             index</li>
		 * 
		 * 
		 */
		public Builder addPersonToGroup(int personIndex, int groupIndex) {
			if (personIndex < 0) {
				throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
			}

			if (groupIndex < 0) {
				throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
			}

			List<Integer> list = data.groupMemberships.get(personIndex);
			if (list == null) {
				list = new ArrayList<>();
				data.groupMemberships.put(personIndex, list);
				data.maxPersonIndex = FastMath.max(data.maxPersonIndex, personIndex);
			}
			if (list.contains(groupIndex)) {
				throw new ContractException(GroupError.DUPLICATE_GROUP_MEMBERSHIP);
			}
			list.add(groupIndex);
			return this;
		}
	}
	
	/**
	 * Returns the number of groups contained in this
	 * {@link BulkGroupMembershipData}
	 */
	public int getGroupCount() {
		return data.groupTypes.size();
	}

	/**
	 * Returns the group type id for the given group index.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError.UNKNOWN_GROUP_ID} if the index is
	 *             < 0</li>
	 *             <li>{@linkplain GroupError.UNKNOWN_GROUP_ID} if the index is
	 *             >= getGroupCount()</li>
	 */
	public GroupTypeId getGroupTypeId(int groupIndex) {
		if (groupIndex < 0) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
		}
		if (groupIndex >= data.groupTypes.size()) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
		}
		return data.groupTypes.get(groupIndex);
	}
	
	public List<GroupTypeId> getGroupTypeIds(){
		return Collections.unmodifiableList(data.groupTypes);
	}

	/**
	 * Returns the list of group indices associated with the given person index.
	 * Returns an empty list if the person is unknown
	 * 
	 */
	public List<Integer> getGroupIndicesForPersonIndex(int personIndex) {
		List<Integer> list = data.groupMemberships.get(personIndex);
		if (list != null) {
			return Collections.unmodifiableList(list);
		}
		return data.emptyGroupIndicesList;
	}

	/**
	 * Returns the people associated with this {@link BulkGroupMembershipData}
	 */
	public List<Integer> getPersonIndices() {
		return new ArrayList<>(data.groupMemberships.keySet());
	}
	
	public Optional<Integer> getMaxPersonIndex() {
		if (data.maxPersonIndex >= 0) {
			return Optional.of(data.maxPersonIndex);
		}
		return Optional.empty();
	}

	public Set<GroupPropertyId> getGroupPropertyIds(int groupIndex) {
		Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupIndex);
		if (map != null) {
			return Collections.unmodifiableSet(map.keySet());
		}
		return data.emptyGroupPropertySet;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getGroupPropertyValue(int groupIndex, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupIndex);
		Object propertyValue = null;
		if (map != null) {
			propertyValue = map.get(groupPropertyId);
		}
		return Optional.ofNullable((T) propertyValue);
	}

}