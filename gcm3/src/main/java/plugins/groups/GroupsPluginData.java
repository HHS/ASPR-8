package plugins.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of groups. It contains: <BR>
 * <ul>
 * <li>group type ids</li>
 * <li>group ids</li>
 * <li>group property definitions: each group type has its own set of properties
 * and all property definitions have default values</li>
 * <li>group property values</li>
 * <li>person group assignments</li>
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GroupsPluginData implements PluginData {

	private static class Data {

		private final Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions;

		private final Map<GroupId, Map<GroupPropertyId, Object>> groupPropertyValues;

		private final Set<GroupTypeId> groupTypeIds;

		private final Set<GroupId> groupIds;

		private final Map<GroupId, GroupTypeId> groupTypes;

		private final List<List<GroupId>> groupMemberships;

		private final List<GroupId> emptyGroupList = Collections.unmodifiableList(new ArrayList<>());

		private int personCount;

		public Data() {
			groupPropertyDefinitions = new LinkedHashMap<>();
			groupPropertyValues = new LinkedHashMap<>();
			groupTypeIds = new LinkedHashSet<>();
			groupIds = new LinkedHashSet<>();
			groupTypes = new LinkedHashMap<>();
			groupMemberships = new ArrayList<>();
		}

		public Data(Data data) {
			personCount = data.personCount;
			groupPropertyDefinitions = new LinkedHashMap<>();
			for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
				Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
				Map<GroupPropertyId, PropertyDefinition> newMap = new LinkedHashMap<>();
				newMap.putAll(map);
				groupPropertyDefinitions.put(groupTypeId, newMap);
			}
			groupPropertyValues = new LinkedHashMap<>();
			for (GroupId groupId : data.groupPropertyValues.keySet()) {
				Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupId);
				Map<GroupPropertyId, Object> newMap = new LinkedHashMap<>();
				newMap.putAll(map);
				groupPropertyValues.put(groupId, newMap);
			}
			groupTypeIds = new LinkedHashSet<>(data.groupTypeIds);
			groupIds = new LinkedHashSet<>(data.groupIds);
			groupTypes = new LinkedHashMap<>(data.groupTypes);

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

	private static void validateGroupPropertyIsNotDefined(Data data, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
		if (map != null) {
			if (map.containsKey(groupPropertyId)) {
				throw new ContractException(GroupError.DUPLICATE_GROUP_PROPERTY_DEFINITION, groupTypeId + ": " + groupPropertyId);
			}
		}
	}

	private static void validatePropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(GroupError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validateGroupPropertyIdNotNull(GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
	}

	private static void validateGroupTypeIdNotNull(GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
	}

	private static void validateGroupTypeDoesNotExist(final Data data, final GroupTypeId groupTypeId) {

		if (data.groupTypeIds.contains(groupTypeId)) {
			throw new ContractException(GroupError.DUPLICATE_GROUP_TYPE, groupTypeId);
		}
	}

	private static void validateGroupPropertyValueNotSet(Data data, GroupId groupId, GroupPropertyId groupPropertyId) {
		Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupId);
		if (map != null) {
			if (map.containsKey(groupPropertyId)) {
				throw new ContractException(GroupError.DUPLICATE_GROUP_PROPERTY_VALUE_ASSIGNMENT, groupId + ": " + groupPropertyId);
			}
		}

	}

	private static void validateGroupPropertyValueNotNull(Object groupPropertyValue) {
		if (groupPropertyValue == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
		}
	}

	private static void validateGroupIdNotNull(GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
	}

	private static void validateGroupDoesNotExist(final Data data, final GroupId groupId) {
		if (data.groupIds.contains(groupId)) {
			throw new ContractException(GroupError.DUPLICATE_GROUP_ID, groupId);
		}
	}

	/**
	 * Builder class for GroupInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginDataBuilder {

		private Data data;
		boolean dataIsMutable;
		
		private void ensureDataMutability() {
			if(!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
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
		 *             <li>{@linkplain PersonError#UNKNOWN_GROUP_TYPE_ID}</li>
		 *             if a group was added with a group type id that was not
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
		 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID}</li>
		 *             if a group property value is added for a group property
		 *             id that is not associated with the group.
		 * 
		 *             <li>{@linkplain GroupError#INCOMPATIBLE_VALUE}</li> if a
		 *             group property value is added that is incompatible with
		 *             the corresponding property definition
		 * 
		 *             <li>{@linkplain PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT}</li>
		 *             if a group property definition does not contain a default
		 *             value
		 * 
		 */
		public GroupsPluginData build() {
			try {
				validate();
				return new GroupsPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds a person to a group
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li> if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
		 *             person id is null
		 * 
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_MEMBERSHIP}</li>
		 *             if the person is already a member of the group
		 * 
		 */
		public Builder addPersonToGroup(final GroupId groupId, final PersonId personId) {
			
			// StopwatchManager.start(Watch.GROUPS_PLUGIN_DATA_INTERNAL);
			ensureDataMutability();
			validateGroupIdNotNull(groupId);
			validatePersonId(personId);
			validatePersonNotInGroup(data, groupId, personId);

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
			groups.add(groupId);
			// StopwatchManager.stop(Watch.GROUPS_PLUGIN_DATA_INTERNAL);
			return this;
		}

		/**
		 * Adds a group type id
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_TYPE}</li> if
		 *             the group type was already added
		 * 
		 */
		public Builder addGroupTypeId(final GroupTypeId groupTypeId) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			validateGroupTypeDoesNotExist(data, groupTypeId);
			data.groupTypeIds.add(groupTypeId);
			return this;
		}

		/**
		 * Adds a group with the given group type
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li> if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_ID}</li> if
		 *             the group was already added
		 * 
		 */
		public Builder addGroup(final GroupId groupId, final GroupTypeId groupTypeId) {
			ensureDataMutability();
			validateGroupIdNotNull(groupId);
			validateGroupTypeIdNotNull(groupTypeId);
			validateGroupDoesNotExist(data, groupId);
			data.groupIds.add(groupId);
			data.groupTypes.put(groupId, groupTypeId);
			return this;
		}

		/**
		 * Defines a group property
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID}</li> if
		 *             the group type id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID}</li>
		 *             if the group property id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_PROPERTY_DEFINITION}</li>
		 *             if the property definition is null
		 *
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_PROPERTY_DEFINITION}
		 *             </li> if a property definition for the given group type
		 *             id and property id was previously defined.
		 * 
		 */
		public Builder defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			validateGroupPropertyIdNotNull(groupPropertyId);
			validatePropertyDefinitionNotNull(propertyDefinition);
			validateGroupPropertyIsNotDefined(data, groupTypeId, groupPropertyId);
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
		 * corresponding property definition
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID}</li>if the
		 *             group id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID}</li>if
		 *             the group property id is null
		 * 
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_VALUE}
		 *             </li>if the group property value is null
		 * 
		 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_PROPERTY_VALUE_ASSIGNMENT}
		 *             </li>if the group property value was previously assigned
		 * 
		 */
		public Builder setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
			ensureDataMutability();
			validateGroupIdNotNull(groupId);
			validateGroupPropertyIdNotNull(groupPropertyId);
			validateGroupPropertyValueNotNull(groupPropertyValue);
			validateGroupPropertyValueNotSet(data, groupId, groupPropertyId);
			Map<GroupPropertyId, Object> propertyMap = data.groupPropertyValues.get(groupId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.groupPropertyValues.put(groupId, propertyMap);
			}
			propertyMap.put(groupPropertyId, groupPropertyValue);
			return this;
		}

		private void validate() {
		
			if(!dataIsMutable) {
				return;
			}
			for (List<GroupId> groupIds : data.groupMemberships) {
				if (groupIds != null) {
					for (GroupId groupId : groupIds) {
						if (!data.groupIds.contains(groupId)) {
							throw new ContractException(GroupError.UNKNOWN_GROUP_ID, "A group membership contains the unknown group " + groupId);
						}
					}
				}
			}
		
			for (GroupId groupId : data.groupTypes.keySet()) {
				GroupTypeId groupTypeId = data.groupTypes.get(groupId);
				if (!data.groupTypeIds.contains(groupTypeId)) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupId + " has unknown group type " + groupTypeId);
				}
			}
		
			for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
				if (!data.groupTypeIds.contains(groupTypeId)) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, "group property definitions have unknown group type " + groupTypeId);
				}
			}
		
			for (GroupId groupId : data.groupPropertyValues.keySet()) {
				if (!data.groupIds.contains(groupId)) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_ID, "A group property value contains the unknown group " + groupId);
				}
				GroupTypeId groupTypeId = data.groupTypes.get(groupId);
		
				Map<GroupPropertyId, PropertyDefinition> propDefMap = data.groupPropertyDefinitions.get(groupTypeId);
				Map<GroupPropertyId, Object> valueMap = data.groupPropertyValues.get(groupId);
				for (GroupPropertyId groupPropertyId : valueMap.keySet()) {
					PropertyDefinition propertyDefinition = propDefMap.get(groupPropertyId);
					if (propertyDefinition == null) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupPropertyId + " under group type " + groupTypeId);
					}
					Object propertyValue = valueMap.get(groupPropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, groupId + ": " + groupPropertyId + ": " + propertyValue);
					}
				}
		
			}
		
			/*
			 * All group property definitions must have default values since groups
			 * may be created dynamically in the simulation
			 */
			for (GroupTypeId groupTypeId : data.groupTypeIds) {
				Map<GroupPropertyId, PropertyDefinition> propertyDefinitionMap = data.groupPropertyDefinitions.get(groupTypeId);
				if (propertyDefinitionMap != null) {
					for (GroupPropertyId groupPropertyId : propertyDefinitionMap.keySet()) {
						PropertyDefinition propertyDefinition = propertyDefinitionMap.get(groupPropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT, groupTypeId + ": " + groupPropertyId);
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
			throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(groupPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupPropertyId);
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
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
		if (!data.groupIds.contains(groupId)) {
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is not associated with the group type id
	 *             via a property definition</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		validateGroupExists(data, groupId);
		validateGroupPropertyIdNotNull(groupPropertyId);

		final GroupTypeId groupTypeId = data.groupTypes.get(groupId);
		validateGroupPropertyIsDefined(data, groupTypeId, groupPropertyId);

		Object result = null;
		final Map<GroupPropertyId, Object> map = data.groupPropertyValues.get(groupId);
		if (map != null) {
			result = map.get(groupPropertyId);
		}
		if (result == null) {
			final Map<GroupPropertyId, PropertyDefinition> defMap = data.groupPropertyDefinitions.get(groupTypeId);
			final PropertyDefinition propertyDefinition = defMap.get(groupPropertyId);
			result = propertyDefinition.getDefaultValue().get();
		}
		return (T) result;

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
	 * Returns the unmodifiable collected group ids
	 */
	public Set<GroupId> getGroupIds() {
		return Collections.unmodifiableSet(data.groupIds);
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
		final GroupTypeId result = data.groupTypes.get(groupId);
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
		if (personIndex < 0 || personIndex >= data.groupMemberships.size()) {
			return data.emptyGroupList;
		}
		List<GroupId> list = data.groupMemberships.get(personIndex);
		if (list == null) {
			return data.emptyGroupList;
		}
		return Collections.unmodifiableList(list);
	}

	private static void validatePersonNotInGroup(Data data, GroupId groupId, PersonId personId) {
		int personIndex = personId.getValue();
		if ((personIndex >= 0) && (personIndex < data.groupMemberships.size())) {
			List<GroupId> groups = data.groupMemberships.get(personIndex);
			if (groups != null) {
				if (groups.contains(groupId)) {
					throw new ContractException(GroupError.DUPLICATE_PERSON_GROUP_ASSIGNMENT, personId + ": " + groupId);
				}
			}
		}
	}

	private static void validatePersonId(PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (personId.getValue() < 0) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
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

}
