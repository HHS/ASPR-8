package plugins.groups.datamanagers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.GuardedBy;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.IdentifiableFunctionMap;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.groups.GroupsPluginData;
import plugins.groups.events.GroupAdditionEvent;
import plugins.groups.events.GroupImminentRemovalEvent;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.events.GroupPropertyDefinitionEvent;
import plugins.groups.events.GroupPropertyUpdateEvent;
import plugins.groups.events.GroupTypeAdditionEvent;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyDefinitionInitialization;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import plugins.util.properties.BooleanPropertyManager;
import plugins.util.properties.DoublePropertyManager;
import plugins.util.properties.EnumPropertyManager;
import plugins.util.properties.FloatPropertyManager;
import plugins.util.properties.IndexedPropertyManager;
import plugins.util.properties.IntPropertyManager;
import plugins.util.properties.ObjectPropertyManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.arraycontainers.IntValueContainer;
import plugins.util.properties.arraycontainers.ObjectValueContainer;
import util.errors.ContractException;

/**
 * <p>
 * Mutable data manager that backs the {@linkplain PersonGroupDataView}.
 * </p>
 *
 * <b>Implementation Notes</b>
 * 
 * <p>
 * Group membership is managed through four mappings: 1)People to Groups,
 * 2)Groups to People, 3)Groups to GroupTypes and 4)GroupTypes to Groups. Except
 * for groups to types, all are implemented as ObjectValueContainers over
 * ArrayLists of Integers rather than as straight-forward maps. This design was
 * chosen to help minimize the memory requirements for storing grouping data for
 * millions of people.
 *
 * The principle assumptions that have driven this design are:
 *
 * 1) The number of people per group is usually fairly small and rarely exceeds
 * 100
 *
 * 2) The number of groups per person is usually very small and rarely exceeds
 * 10
 *
 * 3) The number of group types is fairly small and rarely exceeds 20
 *
 * The mapping from groups to types is accomplished with an IntValueContainer
 * since the number of group types is small and we can treat the group type ids
 * as Bytes or Shorts. The typesToIndexesMap and indexesToTypesMap serve to help
 * convert group-type Object references to and from integers.
 * </p>
 *
 */

public final class GroupsDataManager extends DataManager {

	/*
	 * Used to generate new group id values
	 */
	private int masterGroupId;

	// container for group property values
	private final Map<GroupTypeId, Map<GroupPropertyId, IndexedPropertyManager>> groupPropertyManagerMap = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();

	// Guard for both weights array and weightedPersonIds array
	private boolean samplingIsLocked;

	@GuardedBy("samplingIsLocked")
	// weights array that is reused during sampling
	private double[] weights;

	@GuardedBy("samplingIsLocked")
	// person array that is reused during sampling
	private PersonId[] weightedPersonIds;

	private final ObjectValueContainer typesToGroupsMap = new ObjectValueContainer(null, 0);

	private final ObjectValueContainer groupsToPeopleMap = new ObjectValueContainer(null, 0);

	private final ObjectValueContainer peopleToGroupsMap = new ObjectValueContainer(null, 0);

	private final IntValueContainer groupsToTypesMap = new IntValueContainer(-1);

	private final Map<GroupTypeId, Integer> typesToIndexesMap = new LinkedHashMap<>();

	private final Map<GroupTypeId, Map<GroupPropertyId, Integer>> nonDefaultBearingPropertyIds = new LinkedHashMap<>();

	private Map<GroupTypeId, boolean[]> nonDefaultChecks = new LinkedHashMap<>();

	private final List<GroupTypeId> indexesToTypesMap = new ArrayList<>();

	private StochasticsDataManager stochasticsDataManager;

	private DataManagerContext dataManagerContext;

	private final GroupsPluginData groupsPluginData;

	private PeopleDataManager peopleDataManager;

	/**
	 * Constructs this person group data manager
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT}</li>
	 */
	public GroupsDataManager(GroupsPluginData groupsPluginData) {
		if (groupsPluginData == null) {
			throw new ContractException(GroupError.NULL_GROUP_INITIALIZATION_DATA);
		}
		this.groupsPluginData = groupsPluginData;
	}

	/**
	 * Initial behavior
	 * 
	 * <li>Adds all event labelers defined by the following events</li>
	 * <ul>
	 * <li>{@linkplain GroupMembershipAdditionEvent}</li>
	 * <li>{@linkplain GroupMembershipRemovalEvent}</li>
	 * <li>{@linkplain GroupAdditionEvent}</li>
	 * <li>{@linkplain GroupImminentRemovalEvent}</li>
	 * <li>{@linkplain GroupPropertyUpdateEvent}</li>
	 * </ul>
	 * 
	 * <li>Adds groups, group memberships, group properties from the
	 * {@linkplain GroupsPluginData}</li>
	 * 
	 * 
	 * <li>Subscribes to the following events:
	 * <ul>
	 * 
	 * {@linkplain PersonRemovalEvent} Removes the person from all groups by
	 * scheduling the removal for the current time. This allows references and
	 * group memberships to remain long enough for resolvers, agents and reports
	 * to have final reference to the person while still associated with any
	 * relevant groups.
	 *
	 * 
	 * <BR>
	 * <BR>
	 * Throws {@linkplain ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * </ul>
	 * 
	 * 
	 * </ul>
	 * 
	 * 
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {

		super.init(dataManagerContext);
		if (dataManagerContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		this.dataManagerContext = dataManagerContext;
		stochasticsDataManager = dataManagerContext.getDataManager(StochasticsDataManager.class);
		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);

		dataManagerContext.subscribe(GroupAdditionMutationEvent.class, this::handleGroupAdditionMutationEvent);
		dataManagerContext.subscribe(GroupTypeAdditionMutationEvent.class, this::handleGroupTypeAdditionMutationEvent);
		dataManagerContext.subscribe(GroupMembershipAdditionMutationEvent.class, this::handleGroupMembershipAdditionMutationEvent);
		dataManagerContext.subscribe(GroupPropertyDefinitionMutationEvent.class, this::handleGroupPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(GroupRemovalMutationEvent.class, this::handleGroupRemovalMutationEvent);
		dataManagerContext.subscribe(GroupMembershipRemovalMutationEvent.class, this::handleGroupMembershipRemovalMutationEvent);
		dataManagerContext.subscribe(GroupPropertyUpdateMutationEvent.class, this::handleGroupPropertyUpdateMutationEvent);

		loadGroupTypes();

		loadGroupPropertyDefinitions();

		loadGroups();

		loadGroupMembership();

		loadGroupPropertyValues();

		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();

		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			builder.addGroupTypeId(groupTypeId);
		}

		for (GroupTypeId groupTypeId : groupPropertyDefinitions.keySet()) {
			Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
			for (GroupPropertyId groupPropertyId : map.keySet()) {
				PropertyDefinition propertyDefinition = map.get(groupPropertyId);
				builder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
			}
		}

		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			if (groups != null) {
				for (GroupId groupId : groups) {
					builder.addGroup(groupId, groupTypeId);
				}
			}
		}

		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			if (groups != null) {
				for (GroupId groupId : groups) {
					List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
					if (people != null) {
						for (PersonId personId : people) {
							builder.addPersonToGroup(groupId, personId);
						}
					}
				}
			}
		}

		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			if (groups != null) {
				Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
				for (GroupPropertyId groupPropertyId : map.keySet()) {
					IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
					for (GroupId groupId : groups) {
						Object propertyValue = indexedPropertyManager.getPropertyValue(groupId.getValue());
						builder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
					}
				}
			}
		}

		dataManagerContext.releaseOutput(builder.build());
	}

	private void loadGroupPropertyDefinitions() {
		for (final GroupTypeId groupTypeId : groupsPluginData.getGroupTypeIds()) {
			final Set<GroupPropertyId> propertyIds = groupsPluginData.getGroupPropertyIds(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIds) {
				final PropertyDefinition propertyDefinition = groupsPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				if (propertyDefinition.getDefaultValue().isEmpty()) {
					nonDefaultBearingPropertyIds.get(groupTypeId).put(groupPropertyId, nonDefaultBearingPropertyIds.size());
				}
				Map<GroupPropertyId, IndexedPropertyManager> managerMap = groupPropertyManagerMap.get(groupTypeId);
				Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
				final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(dataManagerContext, propertyDefinition, 0);
				managerMap.put(groupPropertyId, indexedPropertyManager);
				map.put(groupPropertyId, propertyDefinition);
			}
		}
		for (GroupTypeId groupTypeId : nonDefaultBearingPropertyIds.keySet()) {
			Map<GroupPropertyId, Integer> map = nonDefaultBearingPropertyIds.get(groupTypeId);
			nonDefaultChecks.put(groupTypeId, new boolean[map.size()]);
		}
	}

	private void loadGroupTypes() {
		for (final GroupTypeId groupTypeId : groupsPluginData.getGroupTypeIds()) {
			final int index = typesToIndexesMap.size();
			typesToIndexesMap.put(groupTypeId, index);
			indexesToTypesMap.add(groupTypeId);
			groupPropertyManagerMap.put(groupTypeId, new LinkedHashMap<>());
			groupPropertyDefinitions.put(groupTypeId, new LinkedHashMap<>());
			nonDefaultBearingPropertyIds.put(groupTypeId, new LinkedHashMap<>());
		}
	}

	private static record GroupTypeAdditionMutationEvent(GroupTypeId groupTypeId) implements Event {
	}

	/**
	 * Adds a group type id.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_TYPE} if the group
	 *             type id is already present</li>
	 */
	public void addGroupType(GroupTypeId groupTypeId) {
		dataManagerContext.releaseMutationEvent(new GroupTypeAdditionMutationEvent(groupTypeId));
	}

	private void handleGroupTypeAdditionMutationEvent(DataManagerContext dataManagerContext, GroupTypeAdditionMutationEvent groupTypeAdditionMutationEvent) {
		GroupTypeId groupTypeId = groupTypeAdditionMutationEvent.groupTypeId();
		validateGroupTypeIdIsUnknown(groupTypeId);
		final int index = typesToIndexesMap.size();
		typesToIndexesMap.put(groupTypeId, index);
		indexesToTypesMap.add(groupTypeId);
		groupPropertyManagerMap.put(groupTypeId, new LinkedHashMap<>());
		groupPropertyDefinitions.put(groupTypeId, new LinkedHashMap<>());
		nonDefaultBearingPropertyIds.put(groupTypeId, new LinkedHashMap<>());
		nonDefaultChecks.put(groupTypeId, new boolean[0]);

		if (dataManagerContext.subscribersExist(GroupTypeAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupTypeAdditionEvent(groupTypeId));
		}
	}

	private void clearNonDefaultChecks(GroupTypeId groupTypeId) {
		boolean[] checkArray = nonDefaultChecks.get(groupTypeId);
		for (int i = 0; i < checkArray.length; i++) {
			checkArray[i] = false;
		}
	}

	private void markAssigned(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		Integer nonDefaultPropertyIndex = nonDefaultBearingPropertyIds.get(groupTypeId).get(groupPropertyId);
		if (nonDefaultPropertyIndex != null) {
			nonDefaultChecks.get(groupTypeId)[nonDefaultPropertyIndex] = true;
		}
	}

	private void verifyNonDefaultChecks(GroupTypeId groupTypeId) {
		boolean[] checkArray = nonDefaultChecks.get(groupTypeId);
		boolean missingPropertyAssignments = false;

		for (int i = 0; i < checkArray.length; i++) {
			if (!checkArray[i]) {
				missingPropertyAssignments = true;
				break;
			}
		}

		if (missingPropertyAssignments) {
			StringBuilder sb = new StringBuilder();
			int index = -1;
			boolean firstMember = true;
			Map<GroupPropertyId, Integer> map = nonDefaultBearingPropertyIds.get(groupTypeId);
			for (GroupPropertyId groupPropertyId : map.keySet()) {
				index++;
				if (!checkArray[index]) {
					if (firstMember) {
						firstMember = false;
					} else {
						sb.append(", ");
					}
					sb.append(groupPropertyId);
				}
			}
			throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, sb.toString());
		}
	}

	private static record GroupPropertyDefinitionMutationEvent(GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization) implements Event {
	}

	/**
	 * Defines a new group property
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 *
	 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *             if the group property id is already known</li>
	 * 
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the
	 *             groupPropertyDefinitionInitialization contains a property
	 *             assignment for a group that does not exist.
	 * 
	 *             <li>{@linkplain GroupError#INCORRECT_GROUP_TYPE_ID} if the
	 *             groupPropertyDefinitionInitialization contains a property
	 *             assignment for a group that is not of the correct group type.
	 * 
	 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if the groupPropertyDefinitionInitialization does not contain
	 *             property value assignments for every extant group when the
	 *             property definition does not contain a default value
	 * 
	 */
	public void defineGroupProperty(GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization) {
		dataManagerContext.releaseMutationEvent(new GroupPropertyDefinitionMutationEvent(groupPropertyDefinitionInitialization));
	}

	private void handleGroupPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext, GroupPropertyDefinitionMutationEvent groupPropertyDefinitionMutationEvent) {
		GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = groupPropertyDefinitionMutationEvent.groupPropertyDefinitionInitialization;
		GroupTypeId groupTypeId = groupPropertyDefinitionInitialization.getGroupTypeId();
		GroupPropertyId groupPropertyId = groupPropertyDefinitionInitialization.getPropertyId();
		PropertyDefinition propertyDefinition = groupPropertyDefinitionInitialization.getPropertyDefinition();

		validateGroupTypeId(groupTypeId);
		validateNewGroupPropertyId(groupTypeId, groupPropertyId);
		validatePropertyDefinitionNotNull(propertyDefinition);

		int requiredGroupTypeIndex = typesToIndexesMap.get(groupTypeId);

		/*
		 * Validate the contained property value assignments. We do not have to
		 * validate the values since they are guaranteed to be consistent with
		 * the property definition by contract.
		 */
		for (Pair<GroupId, Object> pair : groupPropertyDefinitionInitialization.getPropertyValues()) {
			GroupId groupId = pair.getFirst();
			int gId = groupId.getValue();
			int groupTypeIndex = groupsToTypesMap.getValueAsInt(gId);

			if (groupTypeIndex < 0) {
				throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
			}
			if (groupTypeIndex != requiredGroupTypeIndex) {
				throw new ContractException(GroupError.INCORRECT_GROUP_TYPE_ID, groupId + " is not of type " + groupTypeId);
			}
		}
		/*
		 * Determine whether we will need to check that every group of the given
		 * type has been assigned a value because the property definition does
		 * not contain a default value.
		 */
		boolean checkAllGroupsHaveValues = propertyDefinition.getDefaultValue().isEmpty();
		if (checkAllGroupsHaveValues) {
			/*
			 * create a bit set for tracking which groups received a property
			 * value
			 */
			int idLimit = groupsToTypesMap.size();
			BitSet coverageSet = new BitSet(idLimit);

			/*
			 * record the property values and update the bit set that is
			 * tracking assignment coverate
			 */
			for (Pair<GroupId, Object> pair : groupPropertyDefinitionInitialization.getPropertyValues()) {
				GroupId groupId = pair.getFirst();
				int gId = groupId.getValue();
				coverageSet.set(gId);
			}

			/*
			 * Show that all groups of the of group type did indeed get a value
			 * assignment
			 */
			for (int i = 0; i < idLimit; i++) {
				// only check groups having the correct type
				if (groupsToTypesMap.getValueAsInt(i) == requiredGroupTypeIndex) {
					boolean groupCovered = coverageSet.get(i);
					if (!groupCovered) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
					}
				}
			}
		}

		// integrate the new group property id and definition
		Map<GroupPropertyId, IndexedPropertyManager> managerMap = groupPropertyManagerMap.get(groupTypeId);
		IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(dataManagerContext, propertyDefinition, 0);
		managerMap.put(groupPropertyId, indexedPropertyManager);
		Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		map.put(groupPropertyId, propertyDefinition);

		/*
		 * update internal tracking mechanisms for properties not having default
		 * values
		 */
		if (checkAllGroupsHaveValues) {
			nonDefaultBearingPropertyIds.get(groupTypeId).put(groupPropertyId, nonDefaultBearingPropertyIds.size());
			nonDefaultChecks.put(groupTypeId, new boolean[nonDefaultBearingPropertyIds.size()]);
		}

		for (Pair<GroupId, Object> pair : groupPropertyDefinitionInitialization.getPropertyValues()) {
			GroupId groupId = pair.getFirst();
			int gId = groupId.getValue();
			Object value = pair.getSecond();
			indexedPropertyManager.setPropertyValue(gId, value);
		}

		if (dataManagerContext.subscribersExist(GroupPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupPropertyDefinitionEvent(groupTypeId, groupPropertyId));
		}

	}

	private void validatePropertyDefinitionNotNull(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private void validateNewGroupPropertyId(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		if (map == null || map.containsKey(groupPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION);
		}

	}

	private void validateGroupTypeIdIsUnknown(final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (typesToIndexesMap.keySet().contains(groupTypeId)) {
			throw new ContractException(GroupError.DUPLICATE_GROUP_TYPE);
		}

	}

	private void loadGroupMembership() {
		List<PersonId> people = peopleDataManager.getPeople();
		for (final PersonId personId : people) {
			List<GroupId> groupsForPerson = groupsPluginData.getGroupsForPerson(personId);
			for (final GroupId groupId : groupsForPerson) {

				List<PersonId> peopleForGroup = groupsToPeopleMap.getValue(groupId.getValue());
				if (peopleForGroup == null) {
					peopleForGroup = new ArrayList<>();
					groupsToPeopleMap.setValue(groupId.getValue(), peopleForGroup);
				}
				peopleForGroup.add(personId);

				List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
				if (groups == null) {
					groups = new ArrayList<>(1);
					peopleToGroupsMap.setValue(personId.getValue(), groups);
				}
				groups.add(groupId);
			}
		}
	}

	private static record GroupMembershipAdditionMutationEvent(PersonId personId, GroupId groupId) implements Event {
	}

	/**
	 * Adds a person to a group. Generates the corresponding
	 * {@linkplain GroupMembershipAdditionEvent}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link PersonError#NULL_PERSON_ID} if the person id is
	 *             null</li>
	 *             <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is
	 *             unknown</li>
	 *             <li>{@link GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@link GroupError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown</li>
	 *             <li>{@link GroupError#DUPLICATE_GROUP_MEMBERSHIP} if the
	 *             person is already a member of the group</li>
	 * 
	 * 
	 */
	public void addPersonToGroup(final PersonId personId, final GroupId groupId) {
		dataManagerContext.releaseMutationEvent(new GroupMembershipAdditionMutationEvent(personId, groupId));
	}

	private void handleGroupMembershipAdditionMutationEvent(DataManagerContext dataManagerContext, GroupMembershipAdditionMutationEvent groupMembershipAdditionMutationEvent) {
		PersonId personId = groupMembershipAdditionMutationEvent.personId();
		GroupId groupId = groupMembershipAdditionMutationEvent.groupId();
		validatePersonExists(personId);
		validateGroupExists(groupId);
		validatePersonNotInGroup(personId, groupId);

		List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people == null) {
			people = new ArrayList<>();
			groupsToPeopleMap.setValue(groupId.getValue(), people);
		}
		people.add(personId);

		List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups == null) {
			groups = new ArrayList<>(1);
			peopleToGroupsMap.setValue(personId.getValue(), groups);
		}
		groups.add(groupId);

		if (dataManagerContext.subscribersExist(GroupMembershipAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupMembershipAdditionEvent(personId, groupId));
		}

	}

	/*
	 * Preconditions : the person and group exist
	 */
	private void validatePersonNotInGroup(final PersonId personId, final GroupId groupId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null && groups.contains(groupId)) {
			throw new ContractException(GroupError.DUPLICATE_GROUP_MEMBERSHIP, "Person " + personId + " is already a member of group " + groupId);
		}
	}

	private void loadGroupPropertyValues() {
		for (final GroupId groupId : groupsPluginData.getGroupIds()) {
			final GroupTypeId groupTypeId = groupsPluginData.getGroupTypeId(groupId);
			Map<GroupPropertyId, PropertyDefinition> defMap = groupPropertyDefinitions.get(groupTypeId);
			for (GroupPropertyValue groupPropertyValue : groupsPluginData.getGroupPropertyValues(groupId)) {
				GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
				final Object value = groupPropertyValue.value();
				final PropertyDefinition propertyDefinition = defMap.get(groupPropertyId);
				Object defaultValue = propertyDefinition.getDefaultValue();
				if (!value.equals(defaultValue)) {
					final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
					final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
					indexedPropertyManager.setPropertyValue(groupId.getValue(), value);
				}
			}
		}
	}

	private record GroupPropertyUpdateMutationEvent(GroupId groupId, GroupPropertyId groupPropertyId, Object groupPropertyValue) implements Event {
	}

	/**
	 * Sets a property value for a group. Generates the corresponding
	 * {@linkplain GroupPropertyUpdateEvent} event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError.NULL_GROUP_ID } if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError.UNKNOWN_GROUP_ID } if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PropertyError.NULL_PROPERTY_ID } if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError.UNKNOWN_PROPERTY_ID } if the
	 *             group property id is unknown</li>
	 *             <li>{@linkplain PropertyError.IMMUTABLE_VALUE } if the
	 *             corresponding property definition defines the property as
	 *             immutable</li>
	 *             <li>{@linkplain PropertyError.NULL_PROPERTY_VALUE } if the
	 *             property value is null</li>
	 *             <li>{@linkplain PropertyError.INCOMPATIBLE_VALUE } if
	 *             property value is incompatible with the corresponding
	 *             property definition</li>
	 * 
	 */
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		dataManagerContext.releaseMutationEvent(new GroupPropertyUpdateMutationEvent(groupId, groupPropertyId, groupPropertyValue));
	}

	private void handleGroupPropertyUpdateMutationEvent(DataManagerContext dataManagerContext, GroupPropertyUpdateMutationEvent groupPropertyUpdateMutationEvent) {
		GroupId groupId = groupPropertyUpdateMutationEvent.groupId();
		GroupPropertyId groupPropertyId = groupPropertyUpdateMutationEvent.groupPropertyId();
		Object groupPropertyValue = groupPropertyUpdateMutationEvent.groupPropertyValue();
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = indexesToTypesMap.get(groupsToTypesMap.getValueAsInt(groupId.getValue()));
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		final PropertyDefinition propertyDefinition = groupPropertyDefinitions.get(groupTypeId).get(groupPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateGroupPropertyValueNotNull(groupPropertyValue);
		validateValueCompatibility(groupPropertyId, propertyDefinition, groupPropertyValue);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);

		if (dataManagerContext.subscribersExist(GroupPropertyUpdateEvent.class)) {
			Object oldValue = indexedPropertyManager.getPropertyValue(groupId.getValue());
			indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
			dataManagerContext.releaseObservationEvent(new GroupPropertyUpdateEvent(groupId, groupPropertyId, oldValue, groupPropertyValue));
		} else {
			indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
		}

	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void loadGroups() {
		for (final GroupId groupId : groupsPluginData.getGroupIds()) {
			final GroupTypeId groupTypeId = groupsPluginData.getGroupTypeId(groupId);
			final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			if (groups == null) {
				groups = new ArrayList<>();
				typesToGroupsMap.setValue(typeIndex, groups);
			}
			groups.add(groupId);
			groupsToTypesMap.setIntValue(groupId.getValue(), typeIndex);
		}
		masterGroupId = groupsPluginData.getNextGroupIdValue();
	}

	private static record GroupAdditionMutationEvent(GroupId groupId, GroupConstructionInfo groupConstructionInfo) implements Event {
	}

	/**
	 * Adds groups with any group property initialization that is contained in
	 * the events's auxiliary data. Generates the corresponding
	 * {@linkplain GroupAdditionEvent} event. Returns the id of the first group
	 * added.
	 * 
	 * 
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_CONSTRUCTION_INFO} if
	 *             the group construction info is null</li>
	 *
	 *             <li>{@link GroupError#NULL_GROUP_TYPE_ID} if the group type
	 *             id contained in the group construction info is null</li>
	 * 
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id contained in the group construction info is
	 *             unknown</li>
	 * 
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a group
	 *             property id contained in the group construction info is
	 *             unknown</li>
	 * 
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a group
	 *             property value contained in the group construction info is
	 *             incompatible with the corresponding property definition.</li>
	 * 
	 * 
	 */
	public GroupId addGroup(GroupConstructionInfo groupConstructionInfo) {
		final GroupId groupId = new GroupId(masterGroupId++);
		dataManagerContext.releaseMutationEvent(new GroupAdditionMutationEvent(groupId, groupConstructionInfo));
		return groupId;
	}

	private void handleGroupAdditionMutationEvent(DataManagerContext dataManagerContext, GroupAdditionMutationEvent groupAdditionMutationEvent) {
		GroupConstructionInfo groupConstructionInfo = groupAdditionMutationEvent.groupConstructionInfo();
		GroupId groupId = groupAdditionMutationEvent.groupId();

		validateGroupConstructionInfoNotNull(groupConstructionInfo);
		final GroupTypeId groupTypeId = groupConstructionInfo.getGroupTypeId();
		validateGroupTypeId(groupConstructionInfo.getGroupTypeId());

		// validate the property value assignments included in the event
		final Map<GroupPropertyId, Object> propertyValues = groupConstructionInfo.getPropertyValues();
		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			final PropertyDefinition propertyDefinition = groupPropertyDefinitions.get(groupTypeId).get(groupPropertyId);
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			validateGroupPropertyValueNotNull(groupPropertyValue);
			validateValueCompatibility(groupPropertyId, propertyDefinition, groupPropertyValue);
		}

		/*
		 * If there are properties without default values, then the event must
		 * include value assignments for those properties.
		 */
		boolean checkPropertyCoverage = !nonDefaultBearingPropertyIds.get(groupTypeId).isEmpty();
		if (checkPropertyCoverage) {
			clearNonDefaultChecks(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
				markAssigned(groupTypeId, groupPropertyId);
			}
			verifyNonDefaultChecks(groupTypeId);
		}

		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups == null) {
			groups = new ArrayList<>();
			typesToGroupsMap.setValue(typeIndex, groups);
		}
		groups.add(groupId);
		groupsToTypesMap.setIntValue(groupId.getValue(), typeIndex);

		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
			final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
			indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
		}

		if (dataManagerContext.subscribersExist(GroupAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupAdditionEvent(groupId));
		}

	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateGroupPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	/*
	 * Validates the group type id
	 *
	 * @throws ContractException
	 *
	 * <li>{@link NucleusError#NULL_GROUP_CONSTRUCTION_INFO} if the group
	 * construction info is null
	 */
	private void validateGroupConstructionInfoNotNull(final GroupConstructionInfo groupConstructionInfo) {
		if (groupConstructionInfo == null) {
			throw new ContractException(GroupError.NULL_GROUP_CONSTRUCTION_INFO);
		}

	}

	/**
	 * Adds a group. Generates the corresponding {@linkplain GroupAdditionEvent}
	 * event. Returns the id of the new group.
	 * 
	 * @throws {@link
	 *             ContractException}
	 *
	 *             <li>{@link GroupError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null</li>
	 * 
	 *             <li>{@link GroupError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown</li>
	 *
	 */
	public GroupId addGroup(final GroupTypeId groupTypeId) {
		final GroupId groupId = new GroupId(masterGroupId++);
		dataManagerContext.releaseMutationEvent(new GroupAdditionMutationEvent(groupId, GroupConstructionInfo.builder().setGroupTypeId(groupTypeId).build()));
		return groupId;
	}

	/*
	 * Allocates the weights array to the given size or 50% larger than the
	 * current size, whichever is largest. Size must be non-negative
	 */
	private void allocateWeights(final int size) {
		if (weights == null) {
			weights = new double[size];
			weightedPersonIds = new PersonId[size];
		}
		if (weights.length < size) {
			final int newSize = Math.max(size, weights.length + (weights.length / 2));
			weights = new double[newSize];
			weightedPersonIds = new PersonId[newSize];
		}
	}

	/*
	 * Attempts to acquire a lock on the sampling data structures.
	 * 
	 * @throws ContractException <li>{@linkplain NucleusError#ACCESS_VIOLATION}
	 * If another sampling is ongoing</li>
	 */
	private void aquireSamplingLock() {
		if (samplingIsLocked) {
			throw new ContractException(NucleusError.ACCESS_VIOLATION, "cannot access weighted sampling during the execution of a previous weighted sampling");
		}
		samplingIsLocked = true;
	}

	/*
	 * Returns the index in the weights array that is the first to meet or
	 * exceed the target value. Assumes a strictly increasing set of values for
	 * indices 0 through peopleCount. Decreasing values are strictly prohibited.
	 * Consecutive equal values may return an ambiguous result. The target value
	 * must not exceed weights[peopleCount].
	 *
	 */
	private int findTargetIndex(final double targetValue, final int peopleCount) {
		int low = 0;
		int high = peopleCount - 1;

		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final double midVal = weights[mid];
			if (midVal < targetValue) {
				low = mid + 1;
			} else if (midVal > targetValue) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return low;
	}

	/**
	 * Returns the number of groups there are for a particular group type.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {

		validateGroupTypeId(groupTypeId);

		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	private void validateGroupTypeId(final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (!typesToIndexesMap.keySet().contains(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}

	}

	/**
	 * Returns the number of groups associated with the given person where each
	 * group has the given group type.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		validatePersonExists(personId);
		validateGroupTypeId(groupTypeId);

		int result = 0;
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final GroupTypeId groupType = getGroupType(groupId);
				if (groupType.equals(groupTypeId)) {
					result++;
				}
			}
		}
		return result;
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/**
	 * Returns the number of groups associated with the given person. The person
	 * id must be non-null and non-negative.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public int getGroupCountForPerson(final PersonId personId) {
		validatePersonExists(personId);
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	/**
	 * Returns the set of group ids as a list.
	 */
	public List<GroupId> getGroupIds() {
		final List<GroupId> result = new ArrayList<>();
		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			result.addAll(getGroupsForGroupType(groupTypeId));
		}
		return result;
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
	 *             group property id is unknown</li>
	 */
	public PropertyDefinition getGroupPropertyDefinition(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		validateGroupTypeId(groupTypeId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		final Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		return map.get(groupPropertyId);
	}

	private void validateGroupPropertyId(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		if (map == null || !map.containsKey(groupPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID);
		}

	}

	/**
	 * Returns true if and only if there is a property definition associated
	 * with the given group type id and group property id. Accepts all values.
	 */
	public boolean getGroupPropertyExists(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		if (map == null) {
			return false;
		}
		return map.containsKey(groupPropertyId);
	}

	/**
	 * Returns the set of group property ids for the given group type id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		final Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		final Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (final GroupPropertyId groupPropertyId : map.keySet()) {
			result.add((T) groupPropertyId);
		}
		return result;
	}

	/**
	 * Returns the value of the group property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 * 
	 */
	public double getGroupPropertyTime(final GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = getGroupType(groupId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		return indexedPropertyManager.getPropertyTime(groupId.getValue());
	}

	private void validateGroupExists(final GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}

		if (groupsToTypesMap.getValueAsLong(groupId.getValue()) < 0) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID);
		}
	}

	/**
	 * Returns the value of the group property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 * 
	 */

	public <T> T getGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = getGroupType(groupId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		return indexedPropertyManager.getPropertyValue(groupId.getValue());
	}

	/**
	 * Returns the set groupIds associated with the given group type id as a
	 * list
	 *
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 *
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			return new ArrayList<>(groups);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the set of group ids associated with the given person and group
	 * type as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 * 
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		validatePersonExists(personId);
		validateGroupTypeId(groupTypeId);
		final List<GroupId> result = new ArrayList<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				if (getGroupType(groupId).equals(groupTypeId)) {
					result.add(groupId);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the set group ids associated the the given person as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 * 
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		validatePersonExists(personId);
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return new ArrayList<>(groups);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the group type for the given group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		validateGroupExists(groupId);
		return (T) indexesToTypesMap.get(groupsToTypesMap.getValueAsInt(groupId.getValue()));
	}

	/**
	 * Return the number of group types associated with the person via their
	 * group memberships.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public int getGroupTypeCountForPersonId(final PersonId personId) {
		validatePersonExists(personId);
		final Set<GroupTypeId> types = new LinkedHashSet<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				types.add(getGroupType(groupId));
			}
		}
		return types.size();
	}

	/**
	 * Returns the group type ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		final Set<T> result = new LinkedHashSet<>(typesToIndexesMap.keySet().size());
		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			result.add((T) groupTypeId);
		}
		return result;
	}

	/**
	 * Returns the set group types associated with the person's groups as a
	 * list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		validatePersonExists(personId);
		final Set<T> types = new LinkedHashSet<>();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			for (final GroupId groupId : groups) {
				types.add(getGroupType(groupId));
			}
		}
		return new ArrayList<>(types);
	}

	private IndexedPropertyManager getIndexedPropertyManager(final SimulationContext simulationContext, final PropertyDefinition propertyDefinition, final int intialSize) {

		IndexedPropertyManager indexedPropertyManager;
		if (propertyDefinition.getType() == Boolean.class) {
			indexedPropertyManager = new BooleanPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Float.class) {
			indexedPropertyManager = new FloatPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Double.class) {
			indexedPropertyManager = new DoublePropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Byte.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Short.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Integer.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (propertyDefinition.getType() == Long.class) {
			indexedPropertyManager = new IntPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else if (Enum.class.isAssignableFrom(propertyDefinition.getType())) {
			indexedPropertyManager = new EnumPropertyManager(simulationContext, propertyDefinition, intialSize);
		} else {
			indexedPropertyManager = new ObjectPropertyManager(simulationContext, propertyDefinition, intialSize);
		}
		return indexedPropertyManager;
	}

	/**
	 * Returns the set of people who are in the given group as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		validateGroupExists(groupId);
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return new ArrayList<>(people);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns a list of unique person ids for the given group type(i.e. all
	 * people in groups having that type). Group type id must be valid.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		final Set<PersonId> allPeople = new LinkedHashSet<>();
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					allPeople.addAll(people);
				}
			}
		}
		return new ArrayList<>(allPeople);
	}

	/**
	 * Returns the number of people in the given group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	public int getPersonCountForGroup(final GroupId groupId) {
		validateGroupExists(groupId);
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return people.size();
		}
		return 0;
	}

	/**
	 * Returns the number of people who are associated with groups having the
	 * given group type.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		final Set<PersonId> allPeople = new LinkedHashSet<>();
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					allPeople.addAll(people);
				}
			}
		}
		return allPeople.size();
	}

	/**
	 * Returns true if and only if the group exists. Null tolerant.
	 */
	public boolean groupExists(final GroupId groupId) {
		if ((groupId == null)) {
			return false;
		}
		return groupsToTypesMap.getValueAsLong(groupId.getValue()) >= 0;
	}

	/**
	 * Returns true if and only if the group type exists. Null tolerant.
	 */
	public boolean groupTypeIdExists(final GroupTypeId groupTypeId) {
		return typesToIndexesMap.keySet().contains(groupTypeId);
	}

	/**
	 * Returns true if and only if the person is in the group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public boolean isPersonInGroup(final PersonId personId, final GroupId groupId) {
		validatePersonExists(personId);
		validateGroupExists(groupId);
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return groups.contains(groupId);
		}
		return false;
	}

	private void releaseSamplingLock() {
		if (!samplingIsLocked) {
			throw new RuntimeException("cannot release sample locking when lock not present");
		}
		samplingIsLocked = false;
	}

	private static record GroupRemovalMutationEvent(GroupId groupId) implements Event {
	}

	/**
	 * Removes the group. Generates the corresponding
	 * {@linkplain GroupImminentRemovalEvent} event.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 * 
	 */
	public void removeGroup(final GroupId groupId) {
		dataManagerContext.releaseMutationEvent(new GroupRemovalMutationEvent(groupId));
	}

	private void handleGroupRemovalMutationEvent(DataManagerContext dataManagerContext, GroupRemovalMutationEvent groupRemovalMutationEvent) {
		GroupId groupId = groupRemovalMutationEvent.groupId();
		validateGroupExists(groupId);

		dataManagerContext.addPlan((context) -> {

			final GroupTypeId groupTypeId = getGroupType(groupId);

			final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
			for (final GroupPropertyId groupPropertyId : map.keySet()) {
				final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
				indexedPropertyManager.removeId(groupId.getValue());
			}

			groupsToTypesMap.setIntValue(groupId.getValue(), -1);
			final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			groups.remove(groupId);
			if (groups.size() == 0) {
				typesToGroupsMap.setValue(typeIndex, null);
			}
			final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
			groupsToPeopleMap.setValue(groupId.getValue(), null);
			if (people != null) {
				for (final PersonId personId : people) {
					groups = peopleToGroupsMap.getValue(personId.getValue());
					groups.remove(groupId);
				}
			}

		}, dataManagerContext.getTime());

		if (dataManagerContext.subscribersExist(GroupImminentRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupImminentRemovalEvent(groupId));
		}
	}

	private static record GroupMembershipRemovalMutationEvent(PersonId personId, GroupId groupId) implements Event {
	}

	/**
	 * Removes the person from the group. Generates the corresponding
	 * {@linkplain GroupMembershipRemovalEvent} event.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link PersonError#NULL_PERSON_ID} if the person id is
	 *             null</li>
	 *             <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is
	 *             unknown</li>
	 *             <li>{@link GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@link GroupError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown</li>
	 *             <li>{@link GroupError#NON_GROUP_MEMBERSHIP} if the person is
	 *             not a member of the group</li>
	 * 
	 * 
	 */
	public void removePersonFromGroup(final PersonId personId, final GroupId groupId) {
		dataManagerContext.releaseMutationEvent(new GroupMembershipRemovalMutationEvent(personId, groupId));
	}

	private void handleGroupMembershipRemovalMutationEvent(DataManagerContext dataManagerContext, GroupMembershipRemovalMutationEvent groupMembershipRemovalMutationEvent) {
		PersonId personId = groupMembershipRemovalMutationEvent.personId();
		GroupId groupId = groupMembershipRemovalMutationEvent.groupId();
		validatePersonExists(personId);
		validateGroupExists(groupId);
		validatePersonInGroup(personId, groupId);

		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			people.remove(personId);
			if (people.size() == 0) {
				groupsToPeopleMap.setValue(groupId.getValue(), null);
			}
		}
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			groups.remove(groupId);
		}

		if (dataManagerContext.subscribersExist(GroupMembershipRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new GroupMembershipRemovalEvent(personId, groupId));
		}

	}

	/*
	 * Preconditions : the person and group exist
	 */
	private void validatePersonInGroup(final PersonId personId, final GroupId groupId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups == null || !groups.contains(groupId)) {
			throw new ContractException(GroupError.NON_GROUP_MEMBERSHIP, "Person " + personId + " is not a member of group " + groupId);
		}
	}

	/*
	 * Precondition : group sampler info is not null
	 */
	private void validateGroupSampler(final GroupSampler groupSampler) {
		if (groupSampler == null) {
			throw new ContractException(GroupError.NULL_GROUP_SAMPLER);
		}
		if (groupSampler.getExcludedPerson().isPresent()) {
			final PersonId excludedPersonId = groupSampler.getExcludedPerson().get();
			validatePersonExists(excludedPersonId);
		}
	}

	/**
	 * Returns a randomly selected person from the group using the group sampler
	 * to determine the probability for each person's selection. Returns an
	 * empty optional if no person meets the requirements of the group sampler.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_SAMPLER} if the group
	 *             sampler is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the
	 *             excluded person contained in the sampler is not null and is
	 *             unknown</li>
	 *             <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the sampler contains an unknown random number generator
	 *             id</li>
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId, final GroupSampler groupSampler) {
		validateGroupExists(groupId);
		validateGroupSampler(groupSampler);

		RandomGenerator randomGenerator;
		if (groupSampler.getRandomNumberGeneratorId().isPresent()) {
			final RandomNumberGeneratorId randomNumberGeneratorId = groupSampler.getRandomNumberGeneratorId().get();
			randomGenerator = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		} else {
			randomGenerator = stochasticsDataManager.getRandomGenerator();
		}
		final GroupWeightingFunction groupWeightingFunction = groupSampler.getWeightingFunction().orElse(null);
		final PersonId excludedPersonId = groupSampler.getExcludedPerson().orElse(null);

		final boolean exclude = (excludedPersonId != null) && isPersonInGroup(excludedPersonId, groupId);
		PersonId selectedPersonId = null;
		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());

		if (groupWeightingFunction != null) {

			int candidateCount = people.size();
			if (exclude) {
				candidateCount--;
			}
			if ((people != null) && (candidateCount > 0)) {

				aquireSamplingLock();
				try {
					allocateWeights(people.size());
					/*
					 * Initialize the sum of the weights to zero and set the
					 * index in the weights and weightedPersonId to zero.
					 */
					double sum = 0;
					int weightsLength = 0;
					/*
					 * Collect a weight for each person in the group
					 */
					for (final PersonId personId : people) {
						if (personId.equals(excludedPersonId)) {
							continue;
						}
						/*
						 * Determine the weight of the person. Any weight that
						 * is negative , infinite or NAN is cause to return
						 * immediately since no person may be legitimately
						 * selected.
						 */
						final double weight = groupWeightingFunction.getWeight(dataManagerContext, personId, groupId);
						if (!Double.isFinite(weight) || (weight < 0)) {
							throw new ContractException(GroupError.MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION);

						}
						/*
						 * People having a zero weight are rejected for
						 * selection
						 */
						if (weight > 0) {
							sum += weight;
							weights[weightsLength] = sum;
							weightedPersonIds[weightsLength] = personId;
							weightsLength++;
						}
					}

					/*
					 * If at least one person was accepted for selection, then
					 * we attempt a random selection.
					 */
					if (weightsLength > 0) {
						/*
						 * Although the individual weights may have been finite,
						 * if the sum of those weights is not finite no
						 * legitimate selection can be made
						 */
						if (!Double.isFinite(sum)) {
							throw new ContractException(GroupError.MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION);
						}

						final double targetValue = randomGenerator.nextDouble() * sum;
						final int targetIndex = findTargetIndex(targetValue, weightsLength);
						selectedPersonId = weightedPersonIds[targetIndex];
					}

				} finally {
					releaseSamplingLock();
				}
			}
		} else {
			if (exclude) {
				if ((people != null) && (people.size() > 1)) {
					while (true) {
						final int selectedIndex = randomGenerator.nextInt(people.size());
						final PersonId personId = people.get(selectedIndex);
						if (!personId.equals(excludedPersonId)) {
							selectedPersonId = personId;
							break;
						}
					}
				}
			} else {
				if ((people != null) && (people.size() > 0)) {
					final int selectedIndex = randomGenerator.nextInt(people.size());
					selectedPersonId = people.get(selectedIndex);
				}
			}
		}
		return Optional.ofNullable(selectedPersonId);

	}

	/**
	 * Removes the person from all group tracking.
	 * 
	 */
	private void handlePersonRemovalEvent(final DataManagerContext dataManagerContext, PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.personId();
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		peopleToGroupsMap.setValue(personId.getValue(), null);
		if (groups != null) {
			for (final GroupId groupId : groups) {
				final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
				if (people != null) {
					people.remove(personId);
					if (people.size() == 0) {
						groupsToPeopleMap.setValue(groupId.getValue(), null);
					}
				}
			}
		}
	}

	private static enum GroupAdditionEventFunctionId {
		GROUP_TYPE
	}

	private IdentifiableFunctionMap<GroupAdditionEvent> groupAdditionFunctionMap = //
			IdentifiableFunctionMap	.builder(GroupAdditionEvent.class)//
									.put(GroupAdditionEventFunctionId.GROUP_TYPE, e -> getGroupType(e.groupId()))//
									.build();//

	/**
	 * Returns an event filter used to subscribe to {@link GroupAdditionEvent}
	 * events. Matches on the group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError.NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError.UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 * 
	 */
	public EventFilter<GroupAdditionEvent> getEventFilterForGroupAdditionEvent(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		return EventFilter	.builder(GroupAdditionEvent.class)//
							.addFunctionValuePair(groupAdditionFunctionMap.get(GroupAdditionEventFunctionId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link GroupAdditionEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<GroupAdditionEvent> getEventFilterForGroupAdditionEvent() {

		return EventFilter	.builder(GroupAdditionEvent.class)//
							.build();
	}

	private static enum GroupImminentRemovalEventId {
		GROUP_TYPE, GROUP_ID
	}

	private IdentifiableFunctionMap<GroupImminentRemovalEvent> groupImminentRemovalMap = //
			IdentifiableFunctionMap	.builder(GroupImminentRemovalEvent.class)//
									.put(GroupImminentRemovalEventId.GROUP_TYPE, e -> getGroupType(e.groupId()))//
									.put(GroupImminentRemovalEventId.GROUP_ID, e -> e.groupId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches on the group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 * 
	 */
	public EventFilter<GroupImminentRemovalEvent> getEventFilterForGroupImminentRemovalEvent(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		return EventFilter	.builder(GroupImminentRemovalEvent.class)//
							.addFunctionValuePair(groupImminentRemovalMap.get(GroupImminentRemovalEventId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches on the group id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 * 
	 * 
	 */
	public EventFilter<GroupImminentRemovalEvent> getEventFilterForGroupImminentRemovalEvent(GroupId groupId) {
		validateGroupExists(groupId);
		return EventFilter	.builder(GroupImminentRemovalEvent.class)//
							.addFunctionValuePair(groupImminentRemovalMap.get(GroupImminentRemovalEventId.GROUP_ID), groupId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches all such events.
	 * 
	 */
	public EventFilter<GroupImminentRemovalEvent> getEventFilterForGroupImminentRemovalEvent() {
		return EventFilter	.builder(GroupImminentRemovalEvent.class)//
							.build();
	}

	private static enum GroupMembershipAdditionEventId {
		GROUP_TYPE, GROUP_ID, PERSON_ID;
	}

	private IdentifiableFunctionMap<GroupMembershipAdditionEvent> groupMembershipAdditionMap = //
			IdentifiableFunctionMap	.builder(GroupMembershipAdditionEvent.class)//
									.put(GroupMembershipAdditionEventId.GROUP_TYPE, e -> getGroupType(e.groupId()))//
									.put(GroupMembershipAdditionEventId.GROUP_ID, e -> e.groupId())//
									.put(GroupMembershipAdditionEventId.PERSON_ID, e -> e.personId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent(GroupId groupId) {

		validateGroupExists(groupId);
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.GROUP_ID), groupId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group id and
	 * person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent(GroupId groupId, PersonId personId) {
		validateGroupExists(groupId);
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.GROUP_ID), groupId)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group type id and
	 * person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent(GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(groupTypeId);
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.GROUP_TYPE), groupTypeId)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent(PersonId personId) {
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.addFunctionValuePair(groupMembershipAdditionMap.get(GroupMembershipAdditionEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on all such events.
	 *
	 *
	 * 
	 */
	public EventFilter<GroupMembershipAdditionEvent> getEventFilterForGroupMembershipAdditionEvent() {
		return EventFilter	.builder(GroupMembershipAdditionEvent.class)//
							.build();
	}

	private static enum GroupMembershipRemovalEventId {
		GROUP_TYPE, GROUP_ID, PERSON_ID;
	}

	private IdentifiableFunctionMap<GroupMembershipRemovalEvent> groupMembershipRemovalMap = //
			IdentifiableFunctionMap	.builder(GroupMembershipRemovalEvent.class)//
									.put(GroupMembershipRemovalEventId.GROUP_TYPE, e -> getGroupType(e.groupId()))//
									.put(GroupMembershipRemovalEventId.GROUP_ID, e -> e.groupId())//
									.put(GroupMembershipRemovalEventId.PERSON_ID, e -> e.personId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent(GroupId groupId) {

		validateGroupExists(groupId);
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.GROUP_ID), groupId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group id and
	 * person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent(GroupId groupId, PersonId personId) {
		validateGroupExists(groupId);
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.GROUP_ID), groupId)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group type id and
	 * person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent(GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(groupTypeId);
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.GROUP_TYPE), groupTypeId)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent(PersonId personId) {
		validatePersonExists(personId);
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.addFunctionValuePair(groupMembershipRemovalMap.get(GroupMembershipRemovalEventId.PERSON_ID), personId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on all such events.
	 *
	 *
	 * 
	 */
	public EventFilter<GroupMembershipRemovalEvent> getEventFilterForGroupMembershipRemovalEvent() {
		return EventFilter	.builder(GroupMembershipRemovalEvent.class)//
							.build();
	}

	private static enum GroupPropertyUpdateEventId {
		GROUP_PROPERTY, GROUP_TYPE, GROUP_ID;
	}

	private IdentifiableFunctionMap<GroupPropertyUpdateEvent> groupPropertyUpdateMap = //
			IdentifiableFunctionMap	.builder(GroupPropertyUpdateEvent.class)//
									.put(GroupPropertyUpdateEventId.GROUP_PROPERTY, e -> e.groupPropertyId())//
									.put(GroupPropertyUpdateEventId.GROUP_TYPE, e -> getGroupType(e.groupId()))//
									.put(GroupPropertyUpdateEventId.GROUP_ID, e -> e.groupId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 */
	public EventFilter<GroupPropertyUpdateEvent> getEventFilterForGroupPropertyUpdateEvent(GroupId groupId) {
		validateGroupExists(groupId);
		return EventFilter	.builder(GroupPropertyUpdateEvent.class)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_ID), groupId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group property id and
	 * group id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is not known</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 */
	public EventFilter<GroupPropertyUpdateEvent> getEventFilterForGroupPropertyUpdateEvent(GroupPropertyId groupPropertyId, GroupId groupId) {
		validateGroupExists(groupId);
		GroupTypeId groupTypeId = indexesToTypesMap.get(groupsToTypesMap.getValueAsInt(groupId.getValue()));
		validateGroupPropertyId(groupTypeId, groupPropertyId);

		return EventFilter	.builder(GroupPropertyUpdateEvent.class)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_PROPERTY), groupPropertyId)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_ID), groupId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 */
	public EventFilter<GroupPropertyUpdateEvent> getEventFilterForGroupPropertyUpdateEvent(GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		return EventFilter	.builder(GroupPropertyUpdateEvent.class)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group property id and
	 * group type id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is not known</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 */
	public EventFilter<GroupPropertyUpdateEvent> getEventFilterForGroupPropertyUpdateEvent(GroupPropertyId groupPropertyId, GroupTypeId groupTypeId) {
		validateGroupTypeId(groupTypeId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);

		return EventFilter	.builder(GroupPropertyUpdateEvent.class)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_PROPERTY), groupPropertyId)//
							.addFunctionValuePair(groupPropertyUpdateMap.get(GroupPropertyUpdateEventId.GROUP_TYPE), groupTypeId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on all such events.
	 */
	public EventFilter<GroupPropertyUpdateEvent> getEventFilterForGroupPropertyUpdateEvent() {

		return EventFilter	.builder(GroupPropertyUpdateEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupPropertyDefinitionEvent} events. Matches all such events.
	 * 
	 */
	public EventFilter<GroupPropertyDefinitionEvent> getEventFilterForGroupPropertyDefinitionEvent() {
		return EventFilter	.builder(GroupPropertyDefinitionEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link GroupTypeAdditionEvent} events. Matches all such events.
	 * 
	 */
	public EventFilter<GroupTypeAdditionEvent> getEventFilterForGroupTypeAdditionEvent() {
		return EventFilter	.builder(GroupTypeAdditionEvent.class)//
							.build();
	}
}
