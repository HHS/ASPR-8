package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupPropertyValue;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import net.jcip.annotations.Immutable;

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
 */
@Immutable
public final class GroupsPluginData implements PluginData {

	static class GroupSpecification {
		GroupId groupId;
		GroupTypeId groupTypeId;
		List<GroupPropertyValue> groupPropertyValues;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("GroupSpecification [groupId=");
			builder.append(groupId);
			builder.append(", groupTypeId=");
			builder.append(groupTypeId);
			builder.append(", groupPropertyValues=");
			builder.append(groupPropertyValues);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
			result = prime * result + ((groupPropertyValues == null) ? 0 : groupPropertyValues.hashCode());
			result = prime * result + ((groupTypeId == null) ? 0 : groupTypeId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof GroupSpecification)) {
				return false;
			}
			GroupSpecification other = (GroupSpecification) obj;
			if (groupId == null) {
				if (other.groupId != null) {
					return false;
				}
			} else if (!groupId.equals(other.groupId)) {
				return false;
			}
			if (groupPropertyValues == null) {
				if (other.groupPropertyValues != null) {
					return false;
				}
			} else if (!groupPropertyValues.equals(other.groupPropertyValues)) {
				return false;
			}
			if (groupTypeId == null) {
				if (other.groupTypeId != null) {
					return false;
				}
			} else if (!groupTypeId.equals(other.groupTypeId)) {
				return false;
			}
			return true;
		}

	}

	private static class Data {

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [nextGroupIdValue=");
			builder.append(nextGroupIdValue);
			builder.append(", groupPropertyDefinitions=");
			builder.append(groupPropertyDefinitions);
			builder.append(", groupTypeIds=");
			builder.append(groupTypeIds);
			builder.append(", groupSpecifications=");
			builder.append(groupSpecifications);
			builder.append(", personToGroupsMemberships=");
			builder.append(personToGroupsMemberships);
			builder.append(", groupToPeopleMemberships=");
			builder.append(groupToPeopleMemberships);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((groupPropertyDefinitions == null) ? 0 : groupPropertyDefinitions.hashCode());
			result = prime * result + ((groupSpecifications == null) ? 0 : groupSpecifications.hashCode());
			result = prime * result + ((groupToPeopleMemberships == null) ? 0 : groupToPeopleMemberships.hashCode());
			result = prime * result + ((groupTypeIds == null) ? 0 : groupTypeIds.hashCode());
			result = prime * result + nextGroupIdValue;
			result = prime * result + ((personToGroupsMemberships == null) ? 0 : personToGroupsMemberships.hashCode());
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
			if (!groupPropertyDefinitions.equals(other.groupPropertyDefinitions)
					|| !groupSpecifications.equals(other.groupSpecifications)
					|| !groupToPeopleMemberships.equals(other.groupToPeopleMemberships)
					|| !groupTypeIds.equals(other.groupTypeIds)) {
				return false;
			}
			if (nextGroupIdValue != other.nextGroupIdValue) {
				return false;
			}
			if (!personToGroupsMemberships.equals(other.personToGroupsMemberships)) {
				return false;
			}
			return true;
		}

		private int nextGroupIdValue = -1;
		private Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions;
		private final Set<GroupTypeId> groupTypeIds;
		private final List<GroupId> emptyGroupList;// not part of equals contract
		private final List<PersonId> emptyPersonList;// not part of equals contract

		private boolean locked;// not part of equals contract

		private List<GroupSpecification> groupSpecifications;
		private List<GroupPropertyValue> emptyGroupPropertyValues;// not part of equals contract

		// indexed by person id
		private boolean asymmetricMemberships;// not part of equals contract
		private final List<List<GroupId>> personToGroupsMemberships;
		private final List<List<PersonId>> groupToPeopleMemberships;

		private Data() {
			groupPropertyDefinitions = new LinkedHashMap<>();
			groupTypeIds = new LinkedHashSet<>();
			personToGroupsMemberships = new ArrayList<>();
			groupToPeopleMemberships = new ArrayList<>();
			groupSpecifications = new ArrayList<>();
			emptyGroupList = Collections.unmodifiableList(new ArrayList<>());
			emptyPersonList = Collections.unmodifiableList(new ArrayList<>());
			emptyGroupPropertyValues = Collections.unmodifiableList(new ArrayList<>());
		}

		private Data(Data data) {
			emptyPersonList = Collections.unmodifiableList(new ArrayList<>());
			emptyGroupList = Collections.unmodifiableList(new ArrayList<>());
			emptyGroupPropertyValues = Collections.unmodifiableList(new ArrayList<>());
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

			int n = data.personToGroupsMemberships.size();
			personToGroupsMemberships = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<GroupId> list = data.personToGroupsMemberships.get(i);
				List<GroupId> newList = null;
				if (list != null) {
					newList = new ArrayList<>(list);
				}
				personToGroupsMemberships.add(newList);
			}
			n = data.groupToPeopleMemberships.size();
			groupToPeopleMemberships = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				List<PersonId> list = data.groupToPeopleMemberships.get(i);
				List<PersonId> newList = null;
				if (list != null) {
					newList = new ArrayList<>(list);
				}
				groupToPeopleMemberships.add(newList);
			}

			nextGroupIdValue = data.nextGroupIdValue;

			asymmetricMemberships = data.asymmetricMemberships;

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
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#DUPLICATE_GROUP_MEMBERSHIP}
		 *                           if a person was assigned to a group more than
		 *                           once</li>
		 *                           <li>{@linkplain GroupError#DUPLICATE_GROUP_MEMBERSHIP}
		 *                           if a group was assigned to a person more than
		 *                           once</li>
		 *                           <li>{@linkplain GroupError#GROUP_MEMBERSHIP_ASYMMETRY}
		 *                           if groups and people are not symmetrically
		 *                           assigned</li>
		 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}
		 *                           if a group was added with a group type id that was
		 *                           not defined</li>
		 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}
		 *                           if a group property definition was defined for a
		 *                           group type id that was not defined.</li>
		 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if a
		 *                           group property value was set for a group id that
		 *                           was not defined.</li>
		 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if a
		 *                           group membership was set for a group id that was
		 *                           not defined.</li>
		 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
		 *                           if a group property value is added for a group
		 *                           property id that is not associated with the
		 *                           group.</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
		 *                           if a group property value is added that is
		 *                           incompatible with the corresponding property
		 *                           definition</li>
		 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *                           if a group does not have a group property value
		 *                           assigned when the corresponding property definition
		 *                           lacks a default value.</li>
		 *                           </ul>
		 */
		@Override
		public GroupsPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new GroupsPluginData(data);

		}

		/**
		 * Adds a person to a group and the group to the person. Use this method when
		 * order within memberships is not important.
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
		 *                           group id is null</li>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null</li>
		 *                           </ul>
		 */
		public Builder associatePersonToGroup(final GroupId groupId, final PersonId personId) {

			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validatePersonId(personId);

			int personIndex = personId.getValue();

			while (personIndex >= data.personToGroupsMemberships.size()) {
				data.personToGroupsMemberships.add(null);
			}
			List<GroupId> groups = data.personToGroupsMemberships.get(personIndex);
			if (groups == null) {
				groups = new ArrayList<>();
				data.personToGroupsMemberships.set(personIndex, groups);
			}

			groups.add(groupId);

			int groupIndex = groupId.getValue();

			while (groupIndex >= data.groupToPeopleMemberships.size()) {
				data.groupToPeopleMemberships.add(null);
			}
			List<PersonId> people = data.groupToPeopleMemberships.get(groupIndex);
			if (people == null) {
				people = new ArrayList<>();
				data.groupToPeopleMemberships.set(groupIndex, people);
			}

			people.add(personId);

			return this;
		}

		/**
		 * Adds the group to the person, but not the person to the group.
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
		 *                           group id is null</li>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null
		 *                           </ul>
		 */
		public Builder addGroupToPerson(final GroupId groupId, final PersonId personId) {

			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validatePersonId(personId);

			int personIndex = personId.getValue();

			while (personIndex >= data.personToGroupsMemberships.size()) {
				data.personToGroupsMemberships.add(null);
			}
			List<GroupId> groups = data.personToGroupsMemberships.get(personIndex);
			if (groups == null) {
				groups = new ArrayList<>();
				data.personToGroupsMemberships.set(personIndex, groups);
			}

			data.asymmetricMemberships = true;
			groups.add(groupId);

			return this;
		}

		/**
		 * Adds the group to the person, but not the person to the group.
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
		 *                           group id is null</li>
		 *                           <li>{@linkplain PersonError#NULL_PERSON_ID} if the
		 *                           person id is null
		 *                           </ul>
		 */
		public Builder addPersonToGroup(final PersonId personId, final GroupId groupId) {

			ensureDataMutability();
			validateGroupIdIsLegal(groupId);
			validatePersonId(personId);

			int groupIndex = groupId.getValue();

			while (groupIndex >= data.groupToPeopleMemberships.size()) {
				data.groupToPeopleMemberships.add(null);
			}
			List<PersonId> people = data.groupToPeopleMemberships.get(groupIndex);
			if (people == null) {
				people = new ArrayList<>();
				data.groupToPeopleMemberships.set(groupIndex, people);
			}
			data.asymmetricMemberships = true;
			people.add(personId);

			return this;
		}

		/**
		 * Adds a group type id Duplicate inputs override previous inputs
		 *
		 * @throws ContractException {@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
		 *                           group type id is null
		 */
		public Builder addGroupTypeId(final GroupTypeId groupTypeId) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			data.groupTypeIds.add(groupTypeId);
			return this;
		}

		/**
		 * Adds a group with the given group type Duplicate inputs override previous
		 * inputs
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
		 *                           group id is null</li>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
		 *                           the group type id is null</li>
		 *                           </ul>
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
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
		 *                           the group type id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the group property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if the property definition is null</li>
		 *                           </ul>
		 */
		public Builder defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId,
				final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateGroupTypeIdNotNull(groupTypeId);
			validateGroupPropertyIdNotNull(groupPropertyId);
			validatePropertyDefinitionNotNull(propertyDefinition);
			Map<GroupPropertyId, PropertyDefinition> propertyDefinitionsMap = data.groupPropertyDefinitions
					.get(groupTypeId);
			if (propertyDefinitionsMap == null) {
				propertyDefinitionsMap = new LinkedHashMap<>();
				data.groupPropertyDefinitions.put(groupTypeId, propertyDefinitionsMap);
			}
			propertyDefinitionsMap.put(groupPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Sets the group property value that overrides the default value of the
		 * corresponding property definition Duplicate inputs override previous inputs
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_ID}if the
		 *                           group id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID}if
		 *                           the group property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}if
		 *                           the group property value is null</li>
		 *                           </ul>
		 */
		public Builder setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId,
				final Object value) {
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
		 * Sets the next available group id. This value needs to exceed all extant group
		 * ids. If the nextGroupRecordId is not set explicitly, the nextGroupRecordId is
		 * assigned to either zero or the next integer value that exceeds the highest
		 * valued group added to this builder.
		 *
		 * @throws ContractException {@linkplain GroupError#NEGATIVE_GROUP_ID} if the
		 *                           next group record id is negative
		 */
		public Builder setNextGroupIdValue(int nextGroupIdValue) {
			ensureDataMutability();
			validateGroupIdValue(nextGroupIdValue);
			data.nextGroupIdValue = nextGroupIdValue;
			return this;
		}

		public Builder resetNextGroupIdValue() {
			ensureDataMutability();
			data.nextGroupIdValue = -1;
			return this;
		}

		private void validateData() {

			for (int i = 0; i < data.personToGroupsMemberships.size(); i++) {
				List<GroupId> groupIds = data.personToGroupsMemberships.get(i);
				if (groupIds != null) {

					if (new HashSet<>(groupIds).size() != groupIds.size()) {
						throw new ContractException(GroupError.DUPLICATE_GROUP_MEMBERSHIP,
								new PersonId(i) + " has groups " + groupIds);
					}

					for (GroupId groupId : groupIds) {
						int groupIndex = groupId.getValue();
						if (groupIndex >= data.groupSpecifications.size()) {
							throw new ContractException(GroupError.UNKNOWN_GROUP_ID,
									"A group membership contains the unknown group " + groupId);
						}
						GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
						if (groupSpecification == null) {
							throw new ContractException(GroupError.UNKNOWN_GROUP_ID,
									"A group membership contains the unknown group " + groupId);
						}
					}
				}
			}

			for (int i = 0; i < data.groupToPeopleMemberships.size(); i++) {
				GroupId groupId = new GroupId(i);
				List<PersonId> people = data.groupToPeopleMemberships.get(i);
				if (people != null) {
					int groupIndex = groupId.getValue();
					if (groupIndex >= data.groupSpecifications.size()) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_ID,
								"A group membership contains the unknown group " + groupId);
					}

					GroupSpecification groupSpecification = data.groupSpecifications.get(groupIndex);
					if (groupSpecification == null) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_ID,
								"A group membership contains the unknown group " + groupId);
					}

					if (new HashSet<>(people).size() != people.size()) {
						throw new ContractException(GroupError.DUPLICATE_GROUP_MEMBERSHIP,
								groupId + " has people " + people);
					}
				}
			}

			if (data.asymmetricMemberships) {
				Set<MultiKey> set = new LinkedHashSet<>();

				for (int i = 0; i < data.personToGroupsMemberships.size(); i++) {
					PersonId personId = new PersonId(i);
					List<GroupId> groupIds = data.personToGroupsMemberships.get(i);
					if (groupIds != null) {
						for (GroupId groupId : groupIds) {
							set.add(new MultiKey(personId, groupId));
						}
					}
				}

				for (int i = 0; i < data.groupToPeopleMemberships.size(); i++) {
					GroupId groupId = new GroupId(i);
					List<PersonId> people = data.groupToPeopleMemberships.get(i);
					if (people != null) {
						for (PersonId personId : people) {
							MultiKey multiKey = new MultiKey(personId, groupId);
							if (!set.remove(multiKey)) {
								throw new ContractException(GroupError.GROUP_MEMBERSHIP_ASYMMETRY);
							}
						}
					}
				}

				if (!set.isEmpty()) {
					throw new ContractException(GroupError.GROUP_MEMBERSHIP_ASYMMETRY);
				}

			}

			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					GroupTypeId groupTypeId = groupSpecification.groupTypeId;
					if (groupTypeId == null) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_ID,
								"A group property contains the unknown group " + groupSpecification.groupId);
					}
					if (!data.groupTypeIds.contains(groupTypeId)) {
						throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID,
								groupSpecification.groupId + " has unknown group type " + groupTypeId);
					}
				}
			}

			for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
				if (!data.groupTypeIds.contains(groupTypeId)) {
					throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID,
							"group property definitions have unknown group type " + groupTypeId);
				}
			}

			for (GroupSpecification groupSpecification : data.groupSpecifications) {
				if (groupSpecification != null) {
					GroupTypeId groupTypeId = groupSpecification.groupTypeId;
					Map<GroupPropertyId, PropertyDefinition> propDefMap = data.groupPropertyDefinitions
							.get(groupTypeId);
					if (groupSpecification.groupPropertyValues != null) {
						for (GroupPropertyValue groupPropertyValue : groupSpecification.groupPropertyValues) {
							GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
							PropertyDefinition propertyDefinition = propDefMap.get(groupPropertyId);
							if (propertyDefinition == null) {
								throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID,
										groupPropertyId + " under group type " + groupTypeId);
							}
							Object propertyValue = groupPropertyValue.value();
							if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
								throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
										groupSpecification.groupId + ": " + groupPropertyId + ": " + propertyValue);
							}
						}
					}
				}
			}

			/*
			 * All group property definitions that do not have a default value must have
			 * corresponding property value assignments for added groups.
			 */
			Map<GroupTypeId, Set<GroupPropertyId>> propertyDefsWithoutDefaults = new LinkedHashMap<>();

			for (GroupTypeId groupTypeId : data.groupTypeIds) {
				Set<GroupPropertyId> set = new LinkedHashSet<>();
				propertyDefsWithoutDefaults.put(groupTypeId, set);
				Map<GroupPropertyId, PropertyDefinition> propertyDefinitionMap = data.groupPropertyDefinitions
						.get(groupTypeId);
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
			 * The use of a coverage counter below is dependent on every GroupPropertyValue
			 * associated with a particular group having a unique property id
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
						data.nextGroupIdValue = FastMath.max(data.nextGroupIdValue,
								groupSpecification.groupId.getValue());
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

	private static void validateGroupPropertyIsDefined(final Data data, final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId) {
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
	 *                           <ul>
	 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
	 *                           the group type id is null</li>
	 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}
	 *                           if the group type id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the group property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the group property id is not associated with the
	 *                           group type id via a property definition</li>
	 *                           </ul>
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId,
			final GroupPropertyId groupPropertyId) {
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
	 *                           <ul>
	 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
	 *                           the group type id is null</li>
	 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID}
	 *                           if the group type id is unknown</li>
	 *                           </ul>
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
	 *                           <ul>
	 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
	 *                           group id is null</li>
	 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the
	 *                           group id is unknown</li>
	 *                           </ul>
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
	 *                           <ul>
	 *                           <li>{@linkplain GroupError#NULL_GROUP_ID} if the
	 *                           group id is null</li>
	 *                           <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the
	 *                           group id is unknown</li>
	 *                           </ul>
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
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		if (personId == null) {
			return data.emptyGroupList;
		}
		int personIndex = personId.getValue();
		if (personIndex >= data.personToGroupsMemberships.size()) {
			return data.emptyGroupList;
		}
		List<GroupId> list = data.personToGroupsMemberships.get(personIndex);
		if (list == null) {
			return data.emptyGroupList;
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Returns the unmodifiable list of people associated with the group id
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		if (groupId == null) {
			return data.emptyPersonList;
		}
		int groupIndex = groupId.getValue();
		if (groupIndex >= data.groupToPeopleMemberships.size()) {
			return data.emptyPersonList;
		}
		List<PersonId> list = data.groupToPeopleMemberships.get(groupIndex);
		if (list == null) {
			return data.emptyPersonList;
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
		return data.personToGroupsMemberships.size();
	}

	/**
	 * Returns the int value that exceeds by one the highest group id value
	 * encountered while associating groups with people.
	 */
	public int getGroupCount() {
		return data.groupToPeopleMemberships.size();
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

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	@Override
	public Builder toBuilder() {
		return new Builder(data);
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
		if (!(obj instanceof GroupsPluginData)) {
			return false;
		}
		GroupsPluginData other = (GroupsPluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "GroupsPluginData [" + "data=" + data + ']';
	}

	/**
	 * Returns the next available group id.
	 */
	public int getNextGroupIdValue() {
		return data.nextGroupIdValue;
	}

	public Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> getGroupPropertyDefinitions() {
		Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> result = new LinkedHashMap<>();
		for (GroupTypeId groupTypeId : data.groupPropertyDefinitions.keySet()) {
			Map<GroupPropertyId, PropertyDefinition> map = data.groupPropertyDefinitions.get(groupTypeId);
			Map<GroupPropertyId, PropertyDefinition> newMap = new LinkedHashMap<>(map);
			result.put(groupTypeId, newMap);
		}
		return result;
	}

}
