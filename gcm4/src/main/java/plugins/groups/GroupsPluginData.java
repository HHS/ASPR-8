package plugins.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of the GroupDataManager. It
 * contains: <BR>
 * <ul>
 * <li>group type ids</li>
 * <li>group ids</li>
 * <li>group property definitions: each group type has its own set of properties
 * and all property definitions have default values</li>
 * <li>group property values</li>
 * <li>person group assignments</li>
 * </ul>
 * 
 *
 */
@Immutable
public final class GroupsPluginData implements PluginData {

	private static class GroupSpecification {
		private GroupId groupId;
		private GroupTypeId groupTypeId;
		private List<GroupPropertyValue> groupPropertyValues;
	}

	private static class Data {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + groupTypeIds.hashCode();
			result = prime * result + groupPropertyDefinitions.hashCode();
			result = prime * result + getGroupMembershipsHashCode();
			result = prime * result + getGroupSpecificationsHashCode();
			result = prime * result + nextGroupIdValue;
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

			/*
			 * We exclude:
			 * 
			 * locked -- both datas should be locked when equals is invoked
			 * 
			 * emptyGroupList -- just an empty list
			 * 
			 * emptyGroupPropertyValues-- just an empty list
			 * 
			 * personCount -- this is a convenience value for the client and
			 * does not impact the actual content
			 */

			/*
			 * These are simple comparisons:
			 */
			if (!groupTypeIds.equals(other.groupTypeIds)) {
				return false;
			}
			if (!groupPropertyDefinitions.equals(other.groupPropertyDefinitions)) {
				return false;
			}
			if (nextGroupIdValue !=other.nextGroupIdValue) {
				return false;
			}
			/*
			 * The remaining fields must be compared by disregarding assignments
			 * of default property values and times
			 */

			if (!compareGroupMemberships(this, other)) {
				return false;
			}
			if (!compareGroupSpecifications(this, other)) {
				return false;
			}

			return true;
		}

		private static Map<GroupId, GroupSpecification> getGroupSpecificationMap(Data data) {
			Map<GroupId, GroupSpecification> result = new LinkedHashMap<>();
			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					result.put(groupSpecification.groupId, groupSpecification);
				}
			}
			return result;
		}

		private static Set<GroupPropertyValue> getNonDefaultGroupPropertyValues(Data data, GroupSpecification groupSpecification) {
			Set<GroupPropertyValue> result = new LinkedHashSet<>();
			Map<GroupPropertyId, PropertyDefinition> defMap = data.groupPropertyDefinitions.get(groupSpecification.groupTypeId);
			if (groupSpecification.groupPropertyValues != null) {
				for (GroupPropertyValue groupPropertyValue : groupSpecification.groupPropertyValues) {
					PropertyDefinition propertyDefinition = defMap.get(groupPropertyValue.groupPropertyId());
					boolean valueIsDefault = false;
					Optional<Object> optional = propertyDefinition.getDefaultValue();
					if (optional.isPresent()) {
						if (optional.get().equals(groupPropertyValue.value())) {
							valueIsDefault = true;
						}
					}
					if (!valueIsDefault) {
						result.add(groupPropertyValue);
					}
				}
			}
			return result;
		}

		/*
		 * The GroupSpecifications must represent the same groups with the same
		 * group types. The associated property values must agree on non-default
		 * values.
		 */
		private static boolean compareGroupSpecifications(Data a, Data b) {

			// We place the GroupSpecifications into maps, ignoring null
			// instances
			Map<GroupId, GroupSpecification> aMap = getGroupSpecificationMap(a);
			Map<GroupId, GroupSpecification> bMap = getGroupSpecificationMap(b);

			// They must represent the same groups - without the
			// GroupSpecification, the group does not exist
			if (!aMap.keySet().equals(bMap.keySet())) {
				return false;
			}

			// The groups must be of the same type
			for (GroupId groupId : aMap.keySet()) {
				GroupSpecification aGroupSpecification = aMap.get(groupId);
				GroupSpecification bGroupSpecification = bMap.get(groupId);
				if (!aGroupSpecification.groupTypeId.equals(bGroupSpecification.groupTypeId)) {
					return false;
				}
			}

			// We extract the non-default property values from each group and
			// compare them
			for (GroupId groupId : aMap.keySet()) {
				GroupSpecification aGroupSpecification = aMap.get(groupId);
				GroupSpecification bGroupSpecification = bMap.get(groupId);
				Set<GroupPropertyValue> aGroupPropertyValues = getNonDefaultGroupPropertyValues(a, aGroupSpecification);
				Set<GroupPropertyValue> bGroupPropertyValues = getNonDefaultGroupPropertyValues(b, bGroupSpecification);
				if (!aGroupPropertyValues.equals(bGroupPropertyValues)) {
					return false;
				}
			}

			return true;
		}

		private static boolean compareGroupMemberships(Data a, Data b) {
			int personCount = FastMath.max(a.groupMemberships.size(), b.groupMemberships.size());
			for (int i = 0; i < personCount; i++) {
				Set<GroupId> aSet = getPersonGroupMemberships(a, i);
				Set<GroupId> bSet = getPersonGroupMemberships(b, i);
				if (!aSet.equals(bSet)) {
					return false;
				}
			}
			return true;
		}

		private static Set<GroupId> getPersonGroupMemberships(Data data, int personIndex) {
			Set<GroupId> result = new LinkedHashSet<>();
			if (personIndex < data.groupMemberships.size()) {
				List<GroupId> list = data.groupMemberships.get(personIndex);
				if (list != null) {
					result.addAll(list);
				}
			}
			return result;
		}

		private int getGroupSpecificationsHashCode() {
			int prime = 31;
			int result = 0;
			for (int i = 0; i < groupSpecifications.size(); i++) {
				GroupSpecification groupSpecification = groupSpecifications.get(i);
				if (groupSpecification != null) {
					int subResult = 1;
					subResult = subResult * prime + groupSpecification.groupId.hashCode();
					subResult = subResult * prime + groupSpecification.groupTypeId.hashCode();
					Map<GroupPropertyId, PropertyDefinition> defMap = groupPropertyDefinitions.get(groupSpecification.groupTypeId);
					// the fact that there are group property values ensures us
					// that the defMap is not null
					if (groupSpecification.groupPropertyValues != null) {
						for (GroupPropertyValue groupPropertyValue : groupSpecification.groupPropertyValues) {
							PropertyDefinition propertyDefinition = defMap.get(groupPropertyValue.groupPropertyId());
							boolean isDefaultValue = false;
							Optional<Object> optional = propertyDefinition.getDefaultValue();
							if (optional.isPresent()) {
								Object defaultValue = optional.get();
								if (defaultValue.equals(groupPropertyValue.value())) {
									isDefaultValue = true;
								}
							}
							if (!isDefaultValue) {
								subResult += groupPropertyValue.value().hashCode();
							}
						}
					}
					result += subResult;
				}
			}
			return result;
		}

		private int getGroupMembershipsHashCode() {
			int result = 0;
			for (int i = 0; i < groupMemberships.size(); i++) {
				List<GroupId> list = groupMemberships.get(i);
				if (list != null) {
					for (GroupId groupId : list) {
						result += groupId.hashCode();
					}
				}
			}
			return result;
		}

		private int nextGroupIdValue = -1;
		private final Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions;
		private final Set<GroupTypeId> groupTypeIds;
		private final List<GroupId> emptyGroupList;
		private int personCount;
		private boolean locked;

		private List<GroupSpecification> groupSpecifications;
		private List<GroupPropertyValue> emptyGroupPropertyValues;

		// indexed by person id
		private final List<List<GroupId>> groupMemberships;

		@Override
		public String toString() {
			return "Data{" + "groupPropertyDefinitions=" + groupPropertyDefinitions + ", groupTypeIds=" + groupTypeIds + ", emptyGroupList=" + emptyGroupList + ", personCount=" + personCount
					+ ", locked=" + locked + ", groupSpecifications=" + groupSpecifications + ", emptyGroupPropertyValues=" + emptyGroupPropertyValues + ", groupMemberships=" + groupMemberships + '}';
		}

		public Data() {
			groupPropertyDefinitions = new LinkedHashMap<>();
			groupTypeIds = new LinkedHashSet<>();
			groupMemberships = new ArrayList<>();
			groupSpecifications = new ArrayList<>();
			emptyGroupList = Collections.unmodifiableList(new ArrayList<>());
			emptyGroupPropertyValues = Collections.unmodifiableList(new ArrayList<>());
		}

		public Data(Data data) {
			emptyGroupList = Collections.unmodifiableList(new ArrayList<>());
			emptyGroupPropertyValues = Collections.unmodifiableList(new ArrayList<>());
			personCount = data.personCount;
			groupPropertyDefinitions = new LinkedHashMap<>();
			for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
				Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
				Map<GroupPropertyId, PropertyDefinition> newMap = new LinkedHashMap<>();
				newMap.putAll(map);
				groupPropertyDefinitions.put(groupTypeId, newMap);
			}
			groupTypeIds = new LinkedHashSet<>(data.groupTypeIds);

			groupSpecifications = new ArrayList<>(data.groupSpecifications.size());
			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				GroupSpecification newGroupSpecification = new GroupSpecification();
				newGroupSpecification.groupId = groupSpecification.groupId;
				newGroupSpecification.groupTypeId = groupSpecification.groupTypeId;
				if (groupSpecification.groupPropertyValues != null) {
					newGroupSpecification.groupPropertyValues = new ArrayList<>(groupSpecification.groupPropertyValues);
				}
				groupSpecifications.add(newGroupSpecification);
			}

			int n = data.groupMemberships.size();
			groupMemberships = new ArrayList<>(n);

			for (int i = 0; i < n; i++) {
				List<GroupId> list = data.groupMemberships.get(i);
				List<GroupId> newList = null;
				if (list != null) {
					newList = new ArrayList<>(list);
				}
				groupMemberships.add(newList);
			}

			locked = data.locked;
		}

	}

	private final Data data;

	private GroupsPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validatePropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validateGroupPropertyIdNotNull(GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateGroupTypeIdNotNull(GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
	}

	private static void validateGroupPropertyValueNotNull(Object groupPropertyValue) {
		if (groupPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validateGroupIdIsLegal(GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}

	}

	/**
	 * Builder class for GroupInitialData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {

		private Data data;

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

		private Builder(Data data) {
			this.data = data;

		}

		/**
		 * Return the GroupInitialData from the data collected by this builder.
		 * 
		 * @throws ContractException
		 * 
		 * 
		 * 
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}</li> if
		 *             a group was added with a group type id that was not
		 *             defined
		 * 
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}</li> if
		 *             a group property definition was defined for a group type
		 *             id that was not defined.
		 *
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID}</li> if a
		 *             group property value was set for a group id that was not
		 *             defined.
		 * 
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID}</li> if a
		 *             group membership was set for a group id that was not
		 *             defined.
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}</li>
		 *             if a group property value is added for a group property
		 *             id that is not associated with the group.
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}</li> if
		 *             a group property value is added that is incompatible with
		 *             the corresponding property definition
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a group does not have a group property value assigned
		 *             when the corresponding property definition lacks a
		 *             default value.
		 * 
		 */
		public GroupsPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new GroupsPluginData(data);

		}

		/**
		 * Adds a person to a group Duplicate inputs override previous inputs
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li> if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
		 *             person id is null
		 *
		 *
		 */
		public Builder addPersonToGroup(final GroupId groupId, final PersonId personId) {

			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validatePersonId(personId);

			int personIndex = personId.getValue();
			data.personCount = FastMath.max(data.personCount, personIndex + 1);
			while (personIndex >= data.groupMemberships.size()) {
				data.groupMemberships.add(null);
			}
			List<GroupId> groups = data.groupMemberships.get(personIndex);
			if (groups == null) {
				groups = new ArrayList<>();
				data.groupMemberships.set(personIndex, groups);
			}
			if (!groups.contains(groupId)) {
				groups.add(groupId);
			}
			return this;
		}

		/**
		 * Adds a group type id Duplicate inputs override previous inputs
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 */
		public Builder addGroupTypeId(final GroupTypeId groupTypeId) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			data.groupTypeIds.add(groupTypeId);
			return this;
		}

		/**
		 * Adds a group with the given group type Duplicate inputs override
		 * previous inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li> if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 */
		public Builder addGroup(final GroupId groupId, final GroupTypeId groupTypeId) {
			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validateGroupTypeIdNotNull(groupTypeId);
			int groupIndex = groupId.getValue();
			while (groupIndex >= data.groupSpecifications.size()) {
				data.groupSpecifications.add(null);
			}

			GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
			if (groupSpecification == null) {
				groupSpecification = new GroupSpecification();
				groupSpecification.groupId = groupId;
				data.groupSpecifications.set(groupIndex, groupSpecification);
			}
			groupSpecification.groupTypeId = groupTypeId;
			return this;
		}

		/**
		 * Defines a group property Duplicate inputs override previous inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
		 *             the group property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}</li>
		 *             if the property definition is null
		 *
		 * 
		 */
		public Builder defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			validateGroupPropertyIdNotNull(groupPropertyId);
			validatePropertyDefinitionNotNull(propertyDefinition);
			Map<GroupPropertyId, PropertyDefinition> propertyDefinitionsMap = data.groupPropertyDefinitions.get(groupTypeId);
			if (propertyDefinitionsMap == null) {
				propertyDefinitionsMap = new LinkedHashMap<>();
				data.groupPropertyDefinitions.put(groupTypeId, propertyDefinitionsMap);
			}
			propertyDefinitionsMap.put(groupPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Sets the group property value that overrides the default value of the
		 * corresponding property definition Duplicate inputs override previous
		 * inputs
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li>if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li>if
		 *             the group property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}</li>if
		 *             the group property value is null
		 * 
		 */
		public Builder setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object value) {
			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validateGroupPropertyIdNotNull(groupPropertyId);
			validateGroupPropertyValueNotNull(value);

			int groupIndex = groupId.getValue();
			while (groupIndex >= data.groupSpecifications.size()) {
				data.groupSpecifications.add(null);
			}

			GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
			if (groupSpecification == null) {
				groupSpecification = new GroupSpecification();
				groupSpecification.groupId = groupId;
				data.groupSpecifications.set(groupIndex, groupSpecification);
			}

			List<GroupPropertyValue> groupPropertyValues = groupSpecification.groupPropertyValues;
			if (groupPropertyValues == null) {
				groupPropertyValues = new ArrayList<>();
				groupSpecification.groupPropertyValues = groupPropertyValues;
			}

			Iterator<GroupPropertyValue> iterator = groupSpecification.groupPropertyValues.iterator();
			while (iterator.hasNext()) {
				GroupPropertyValue next = iterator.next();
				if (next.groupPropertyId().equals(groupPropertyId)) {
					iterator.remove();
					break;
				}
			}

			GroupPropertyValue groupPropertyValue = new GroupPropertyValue(groupPropertyId, value);
			groupPropertyValues.add(groupPropertyValue);

			return this;
		}

		/**
		 * Sets the next available group id. This value needs to exceed all
		 * extant group ids. If the nextGroupRecordId is not set explicitly, the
		 * nextGroupRecordId is assigned to either zero or the next integer
		 * value that exceeds the highest valued group added to this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NEGATIVE_GROUP_ID} if the next
		 *             group record id is negative</li>
		 * 
		 */
		public Builder setNextGroupIdValue(int nextGroupIdValue) {
			ensureDataMutability();
			validateGroupIdValue(nextGroupIdValue);
			data.nextGroupIdValue = nextGroupIdValue;
			return this;
		}

		private void validateData() {

			for (List<GroupId> groupIds : data.groupMemberships) {
				if (groupIds != null) {
					for (GroupId groupId : groupIds) {
						int groupIndex = groupId.getValue();
						if (groupIndex >= data.groupSpecifications.size()) {
							throw new ContractException(GroupError.UNKNOWN_GROUP_ID, "A group membership contains the unknown group " + groupId);
						}
						GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
						if (groupSpecification == null) {
							throw new ContractException(GroupError.UNKNOWN_GROUP_ID, "A group membership contains the unknown group " + groupId);
						}
					}
				}
			}

			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					GroupTypeId groupTypeId = groupSpecification.groupTypeId;
					if (groupTypeId == null) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_ID, "A group property contains the unknown group " + groupSpecification.groupId);
					}
					if (!data.groupTypeIds.contains(groupTypeId)) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupSpecification.groupId + " has unknown group type " + groupTypeId);
					}
				}
			}

			for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
				if (!data.groupTypeIds.contains(groupTypeId)) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, "group property definitions have unknown group type " + groupTypeId);
				}
			}

			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					GroupTypeId groupTypeId = groupSpecification.groupTypeId;
					Map<GroupPropertyId, PropertyDefinition> propDefMap = data.groupPropertyDefinitions.get(groupTypeId);
					if (groupSpecification.groupPropertyValues != null) {
						for (GroupPropertyValue groupPropertyValue : groupSpecification.groupPropertyValues) {
							GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
							PropertyDefinition propertyDefinition = propDefMap.get(groupPropertyId);
							if (propertyDefinition == null) {
								throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, groupPropertyId + " under group type " + groupTypeId);
							}
							Object propertyValue = groupPropertyValue.value();
							if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
								throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, groupSpecification.groupId + ": " + groupPropertyId + ": " + propertyValue);
							}
						}
					}
				}
			}

			/*
			 * All group property definitions that do not have a default value
			 * must have corresponding property value assignments for added
			 * groups.
			 */

			Map<GroupTypeId, Set<GroupPropertyId>> propertyDefsWithoutDefaults = new LinkedHashMap<>();

			for (GroupTypeId groupTypeId : data.groupTypeIds) {
				Set<GroupPropertyId> set = new LinkedHashSet<>();
				propertyDefsWithoutDefaults.put(groupTypeId, set);
				Map<GroupPropertyId, PropertyDefinition> propertyDefinitionMap = data.groupPropertyDefinitions.get(groupTypeId);
				if (propertyDefinitionMap != null) {
					for (GroupPropertyId groupPropertyId : propertyDefinitionMap.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitionMap.get(groupPropertyId);
						if (propertyDefinition.getDefaultValue().isEmpty()) {
							set.add(groupPropertyId);
						}
					}
				}
			}

			/*
			 * The use of a coverage counter below is dependent on every
			 * GroupPropertyValue associated with a particular group having a
			 * unique property id
			 */
			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					Set<GroupPropertyId> set = propertyDefsWithoutDefaults.get(groupSpecification.groupTypeId);
					if (set.size() > 0) {
						int coverageCount = 0;
						if (groupSpecification.groupPropertyValues != null) {
							for (GroupPropertyValue groupPropertyValue : groupSpecification.groupPropertyValues) {
								if (set.contains(groupPropertyValue.groupPropertyId())) {
									coverageCount++;
								}
							}
						}
						if (coverageCount != set.size()) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
						}
					}
				}
			}

			if (data.nextGroupIdValue < 0) {
				for (GroupSpecification groupSpecification : data.groupSpecifications) {
					if (groupSpecification != null) {
						data.nextGroupIdValue = FastMath.max(data.nextGroupIdValue, groupSpecification.groupId.getValue());
					}
				}
				data.nextGroupIdValue++;
			} else {
				for (GroupSpecification groupSpecification : data.groupSpecifications) {
					if (groupSpecification != null) {
						if (groupSpecification.groupId.getValue() >= data.nextGroupIdValue) {
							throw new ContractException(GroupError.NEXT_GROUP_ID_TOO_SMALL);
						}
					}
				}

			}
		}
	}

	private static void validateGroupTypeExists(final Data data, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		if (!data.groupTypeIds.contains(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static void validateGroupPropertyIsDefined(final Data data, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		final Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
		if (map == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, groupPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, groupPropertyId);
		}
	}

	/**
	 * Returns the property definition for the given group type id and group
	 * property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is not associated with the group type id
	 *             via a property definition</li>
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		validateGroupTypeExists(data, groupTypeId);
		validateGroupPropertyIdNotNull(groupPropertyId);
		validateGroupPropertyIsDefined(data, groupTypeId, groupPropertyId);
		final Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
		final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
		return propertyDefinition;
	}

	/**
	 * Returns the set of group property ids for the given group type id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
		validateGroupTypeExists(data, groupTypeId);
		final Set<T> result = new LinkedHashSet<>();
		final Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
		if (map != null) {
			for (GroupPropertyId groupPropertyId : map.keySet()) {
				result.add((T) groupPropertyId);
			}
		}
		return result;
	}

	private static void validateGroupExists(final Data data, final GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
		int groupIndex = groupId.getValue();
		if (groupIndex >= data.groupSpecifications.size()) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
		GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);

		if (groupSpecification == null) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	/**
	 * Returns the property value associated with the given group id and group
	 * property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 * 
	 */
	public List<GroupPropertyValue> getGroupPropertyValues(final GroupId groupId) {
		validateGroupExists(data, groupId);
		int groupIndex = groupId.getValue();
		GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
		if (groupSpecification.groupPropertyValues == null) {
			return data.emptyGroupPropertyValues;
		}
		return Collections.unmodifiableList(groupSpecification.groupPropertyValues);
	}

	/**
	 * Returns the set of group type ids
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		Set<T> result = new LinkedHashSet<>(data.groupTypeIds.size());
		for (GroupTypeId groupTypeId : data.groupTypeIds) {
			result.add((T) groupTypeId);
		}
		return result;
	}

	/**
	 * Returns the group ids as a list
	 */
	public List<GroupId> getGroupIds() {
		List<GroupId> result = new ArrayList<>();
		for (GroupSpecification groupSpecification : data.groupSpecifications) {
			if (groupSpecification != null) {
				result.add(groupSpecification.groupId);
			}
		}
		return result;
	}

	/**
	 * Returns the group type id associated with the given group id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupTypeId(final GroupId groupId) {
		validateGroupExists(data, groupId);
		int groupIndex = groupId.getValue();
		GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
		final GroupTypeId result = groupSpecification.groupTypeId;
		return (T) result;
	}

	/**
	 * Returns the unmodifiable list of groups associated with the person id
	 * 
	 * 
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		if (personId == null) {
			return data.emptyGroupList;
		}
		int personIndex = personId.getValue();
		if (personIndex >= data.groupMemberships.size()) {
			return data.emptyGroupList;
		}
		List<GroupId> list = data.groupMemberships.get(personIndex);
		if (list == null) {
			return data.emptyGroupList;
		}
		return Collections.unmodifiableList(list);
	}

	private static void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validateGroupIdValue(int groupIdValue) {
		if (groupIdValue < 0) {
			throw new ContractException(GroupError.NEGATIVE_GROUP_ID, groupIdValue);
		}
	}

	/**
	 * Returns the int value that exceeds by one the highest person id value
	 * encountered while associating people with groups.
	 */
	public int getPersonCount() {
		return data.personCount;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GroupsPluginData))
			return false;
		GroupsPluginData that = (GroupsPluginData) o;
		return data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	@Override
	public String toString() {
		return "GroupsPluginData{" + "data=" + data + '}';
	}

	/**
	 * Returns the next available group id.
	 */
	public int getNextGroupIdValue() {
		return data.nextGroupIdValue;
	}

}
