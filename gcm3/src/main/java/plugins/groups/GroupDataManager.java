package plugins.groups;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.GuardedBy;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.events.GroupAdditionEvent;
import plugins.groups.events.GroupImminentRemovalEvent;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.events.GroupPropertyUpdateEvent;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.people.PersonDataManager;
import plugins.people.events.BulkPersonAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.support.BulkPersonConstructionData;
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

/**
 * <p>
 * Mutable data manager that backs the {@linkplain PersonGroupDataView}. This
 * data manager is for internal use by the {@link CompartmentPlugin} and should
 * not be published.
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
 * @author Shawn Hatch
 */

public final class GroupDataManager extends DataManager {

	/*
	 * Used to generate new group id values
	 */
	private int masterGroupId;

	// container for group property values
	private final Map<GroupTypeId, Map<GroupPropertyId, IndexedPropertyManager>> groupPropertyManagerMap = new LinkedHashMap<>();

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

	private final Map<GroupTypeId, Map<GroupPropertyId, PropertyDefinition>> groupPropertyDefinitions = new LinkedHashMap<>();

	private final List<GroupTypeId> indexesToTypesMap = new ArrayList<>();

	private StochasticsDataManager stochasticsDataManager;

	private DataManagerContext dataManagerContext;

	private final GroupPluginData groupPluginData;

	/**
	 * Constructs this person group data manager
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT}</li>
	 */
	public GroupDataManager(GroupPluginData groupPluginData) {
		if (groupPluginData == null) {
			throw new ContractException(GroupError.NULL_GROUP_INITIALIZATION_DATA);
		}
		this.groupPluginData = groupPluginData;
	}

	private PersonDataManager personDataManager;

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
	 * {@linkplain GroupPluginData}</li>
	 * 
	 * 
	 * <li>Subscribes to the following events:
	 * <ul>
	 * 
	 * {@linkplain BulkPersonAdditionEvent} Assigns the newly created
	 * people into newly created groups on the basis of auxiliary data carried
	 * in the event as a BulkGroupMembershipData. Publishes the groups to the
	 * person group data view. Generates the corresponding
	 * {@linkplain GroupAdditionEvent} events. <BR>
	 * <BR>
	 * Throws {@link ContractException}
	 * <ul>
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the event contains an
	 * unknown person id</li>
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the BulkMembership data
	 * exists and contains an unknown person id, i.e. it uses a person
	 * index</li>
	 * <li>{@link GroupError#UNKNOWN_GROUP_TYPE_ID} if the BulkMembership data
	 * exists and contains an unknown group type id</li>
	 * </ul>
	 * 
	 * {@linkplain PersonImminentRemovalEvent} Removes the person
	 * from all groups by scheduling the removal for the current time. This
	 * allows references and group memberships to remain long enough for
	 * resolvers, agents and reports to have final reference to the person while
	 * still associated with any relevant groups.
	 * {@linkplain CompartmentDataManager}
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
		stochasticsDataManager = dataManagerContext.getDataManager(StochasticsDataManager.class).get();
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class).get();

		/*
		 * Add the event labelers associated with the various observation events
		 * contained in the groups plugin.
		 */

		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForAll());
		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForGroup());
		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForGroupAndPerson());
		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForGroupType(this));
		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForGroupTypeAndPerson(this));
		dataManagerContext.addEventLabeler(GroupMembershipAdditionEvent.getEventLabelerForPerson());

		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForAll());
		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForGroup());
		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForGroupAndPerson());
		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForGroupType(this));
		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForGroupTypeAndPerson(this));
		dataManagerContext.addEventLabeler(GroupMembershipRemovalEvent.getEventLabelerForPerson());

		dataManagerContext.addEventLabeler(GroupAdditionEvent.getEventLabelerForAll());
		dataManagerContext.addEventLabeler(GroupAdditionEvent.getEventLabelerForGroupType(this));

		dataManagerContext.addEventLabeler(GroupImminentRemovalEvent.getEventLabelerForAll());
		dataManagerContext.addEventLabeler(GroupImminentRemovalEvent.getEventLabelerForGroup());
		dataManagerContext.addEventLabeler(GroupImminentRemovalEvent.getEventLabelerForGroupType(this));

		dataManagerContext.addEventLabeler(GroupPropertyUpdateEvent.getEventLabelerForAll());
		dataManagerContext.addEventLabeler(GroupPropertyUpdateEvent.getEventLabelerForGroup());
		dataManagerContext.addEventLabeler(GroupPropertyUpdateEvent.getEventLabelerForGroupAndProperty());
		dataManagerContext.addEventLabeler(GroupPropertyUpdateEvent.getEventLabelerForGroupType(this));
		dataManagerContext.addEventLabeler(GroupPropertyUpdateEvent.getEventLabelerForGroupTypeAndProperty(this));

		loadGroupTypes();
		loadGroupPropertyDefinitions();
		loadGroups();
		loadGroupMembership();
		loadGroupPropertyValues();

		dataManagerContext.subscribe(BulkPersonAdditionEvent.class, this::handleBulkPersonAdditionEvent);
		dataManagerContext.subscribe(PersonImminentRemovalEvent.class, this::handlePersonImminentRemovalEvent);

	}

	private void loadGroupPropertyDefinitions() {
		for (final GroupTypeId groupTypeId : groupPluginData.getGroupTypeIds()) {
			final Set<GroupPropertyId> propertyIds = groupPluginData.getGroupPropertyIds(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIds) {
				final PropertyDefinition propertyDefinition = groupPluginData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				Map<GroupPropertyId, IndexedPropertyManager> managerMap = groupPropertyManagerMap.get(groupTypeId);
				Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
				final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(dataManagerContext, propertyDefinition, 0);
				managerMap.put(groupPropertyId, indexedPropertyManager);
				map.put(groupPropertyId, propertyDefinition);
			}
		}
	}

	private void loadGroupTypes() {
		for (final GroupTypeId groupTypeId : groupPluginData.getGroupTypeIds()) {
			final int index = typesToIndexesMap.size();
			typesToIndexesMap.put(groupTypeId, index);
			indexesToTypesMap.add(groupTypeId);
			groupPropertyManagerMap.put(groupTypeId, new LinkedHashMap<>());
			groupPropertyDefinitions.put(groupTypeId, new LinkedHashMap<>());
		}
	}

	private void loadGroupMembership() {
		for (final GroupId groupId : groupPluginData.getGroupIds()) {
			for (final PersonId personId : groupPluginData.getGroupMembers(groupId)) {


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
			}
		}
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

		dataManagerContext.releaseEvent(new GroupMembershipAdditionEvent(personId, groupId));
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
		for (final GroupId groupId : groupPluginData.getGroupIds()) {
			final GroupTypeId groupTypeId = groupPluginData.getGroupTypeId(groupId);
			for (final GroupPropertyId groupPropertyId : groupPluginData.getGroupPropertyIds(groupTypeId)) {
				final Object groupPropertyValue = groupPluginData.getGroupPropertyValue(groupId, groupPropertyId);
				final PropertyDefinition propertyDefinition = groupPropertyDefinitions.get(groupTypeId).get(groupPropertyId);
				Object defaultValue = propertyDefinition.getDefaultValue().get();
				if (!groupPropertyValue.equals(defaultValue)) {
					final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
					final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
					indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
				}
			}
		}
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
	 *             <li>{@linkplain GroupError.NULL_GROUP_PROPERTY_ID } if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError.UNKNOWN_GROUP_PROPERTY_ID } if the
	 *             group property id is unknown</li>
	 *             <li>{@linkplain PropertyError.IMMUTABLE_VALUE } if the
	 *             corresponding property definition defines the property as
	 *             immutable</li>
	 *             <li>{@linkplain GroupError.NULL_GROUP_PROPERTY_VALUE } if the
	 *             property value is null</li>
	 *             <li>{@linkplain PropertyError.INCOMPATIBLE_VALUE } if
	 *             property value is incompatible with the corresponding
	 *             property definition</li>
	 * 
	 */
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = indexesToTypesMap.get(groupsToTypesMap.getValueAsInt(groupId.getValue()));
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		final PropertyDefinition propertyDefinition = groupPropertyDefinitions.get(groupTypeId).get(groupPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateGroupPropertyValueNotNull(groupPropertyValue);
		validateValueCompatibility(groupPropertyId, propertyDefinition, groupPropertyValue);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		Object oldValue = indexedPropertyManager.getPropertyValue(groupId.getValue());
		indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
		dataManagerContext.releaseEvent(new GroupPropertyUpdateEvent(groupId, groupPropertyId, oldValue, groupPropertyValue));
	}

	private static void validateGroupPropertyValueNotNull(Object groupPropertyValue) {
		if (groupPropertyValue == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void loadGroups() {
		masterGroupId = -1;
		for (final GroupId groupId : groupPluginData.getGroupIds()) {
			final GroupTypeId groupTypeId = groupPluginData.getGroupTypeId(groupId);
			final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
			List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
			if (groups == null) {
				groups = new ArrayList<>();
				typesToGroupsMap.setValue(typeIndex, groups);
			}
			groups.add(groupId);
			masterGroupId = FastMath.max(masterGroupId, groupId.getValue());
			groupsToTypesMap.setIntValue(groupId.getValue(), typeIndex);
		}
		masterGroupId++;
	}

	/**
	 * Adds groups with any group property initialization that is contained in
	 * the events's auxiliary data. Generates the corresponding
	 * {@linkplain GroupAdditionEvent} event. Returns the id of the
	 * first group added.
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
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if a
	 *             group property id contained in the group construction info is
	 *             unknown</li>
	 * 
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a group
	 *             property value contained in the group construction info is
	 *             incompatible with the corresponding property definition.</li>
	 * 
	 * 
	 */
	public GroupId addGroup(GroupConstructionInfo groupConstructionInfo) {
		validateGroupConstructionInfoNotNull(dataManagerContext, groupConstructionInfo);
		final GroupTypeId groupTypeId = groupConstructionInfo.getGroupTypeId();
		validateGroupTypeId(dataManagerContext, groupConstructionInfo.getGroupTypeId());

		final Map<GroupPropertyId, Object> propertyValues = groupConstructionInfo.getPropertyValues();
		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			validateGroupPropertyId(groupTypeId, groupPropertyId);
			final PropertyDefinition propertyDefinition = groupPropertyDefinitions.get(groupTypeId).get(groupPropertyId);
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			validateGroupPropertyValueNotNull(dataManagerContext, groupPropertyValue);
			validateValueCompatibility(groupPropertyId, propertyDefinition, groupPropertyValue);
		}

		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups == null) {
			groups = new ArrayList<>();
			typesToGroupsMap.setValue(typeIndex, groups);
		}
		final GroupId groupId = new GroupId(masterGroupId++);

		groups.add(groupId);
		groupsToTypesMap.setIntValue(groupId.getValue(), typeIndex);

		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
			final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
			indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
		}
		dataManagerContext.releaseEvent(new GroupAdditionEvent(groupId));
		return groupId;
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateGroupPropertyValueNotNull(final DataManagerContext dataManagerContext, final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
		}
	}

	/*
	 * Validates the group type id
	 *
	 * @throws ContractException
	 *
	 * <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type id is null
	 *
	 * <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group type id is
	 * unknown
	 */
	private void validateGroupTypeId(final DataManagerContext dataManagerContext, final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (!typesToIndexesMap.keySet().contains(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
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
	private void validateGroupConstructionInfoNotNull(final DataManagerContext dataManagerContext, final GroupConstructionInfo groupConstructionInfo) {
		if (groupConstructionInfo == null) {
			throw new ContractException(GroupError.NULL_GROUP_CONSTRUCTION_INFO);
		}

	}

	/**
	 * Adds a group. Generates the corresponding
	 * {@linkplain GroupAdditionEvent} event. Returns the id of the
	 * new group.
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
		validateGroupTypeId(groupTypeId);

		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups == null) {
			groups = new ArrayList<>();
			typesToGroupsMap.setValue(typeIndex, groups);
		}
		final GroupId result = new GroupId(masterGroupId++);

		groups.add(result);
		groupsToTypesMap.setIntValue(result.getValue(), typeIndex);

		dataManagerContext.releaseEvent(new GroupAdditionEvent(result));

		return result;
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
		if (!personDataManager.personExists(personId)) {
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
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
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}

		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		if (map == null || !map.containsKey(groupPropertyId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID);
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
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

		if (groupId.getValue() < 0 || groupsToTypesMap.getValueAsLong(groupId.getValue()) < 0) {
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
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
		if ((groupId == null) || (groupId.getValue() < 0)) {
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
	public boolean isPersonInGroup(final PersonId personId,final GroupId groupId) {
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

		validateGroupExists(groupId);
		dataManagerContext.releaseEvent(new GroupImminentRemovalEvent(groupId));
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
	 *             <li>{@link GroupError#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown</li>
	 *             <li>{@link GroupError#NON_GROUP_MEMBERSHIP} if the person is
	 *             not a member of the group</li>
	 * 
	 * 
	 */
	public void removePersonFromGroup(final PersonId personId,final GroupId groupId) {

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

		dataManagerContext.releaseEvent(new GroupMembershipRemovalEvent(personId, groupId));
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

		final boolean exclude = (excludedPersonId != null) && isPersonInGroup(excludedPersonId,groupId);
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

	private void validatePersonIndexExists(final int personIndex) {
		if (!personDataManager.personIndexExists(personIndex)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void handleBulkPersonAdditionEvent(final DataManagerContext dataManagerContext, final BulkPersonAdditionEvent bulkPersonAdditionEvent) {
		BulkPersonConstructionData bulkPersonConstructionData = bulkPersonAdditionEvent.getBulkPersonConstructionData();
		Optional<BulkGroupMembershipData> optional = bulkPersonConstructionData.getValue(BulkGroupMembershipData.class);
		if (optional.isPresent()) {
			int personCount = bulkPersonConstructionData.getPersonConstructionDatas().size();			
			int basePersonIndex = bulkPersonAdditionEvent.getPersonId().getValue();

			for (int i = 0; i < personCount; i++) {
				validatePersonIndexExists(i + basePersonIndex);
			}
			BulkGroupMembershipData bulkGroupMembershipData = optional.get();
			int groupCount = bulkGroupMembershipData.getGroupCount();
			for (int i = 0; i < groupCount; i++) {
				GroupTypeId groupTypeId = bulkGroupMembershipData.getGroupTypeId(i);
				validateGroupTypeId(dataManagerContext, groupTypeId);
			}
			for (Integer personIndex : bulkGroupMembershipData.getPersonIndices()) {
				validatePersonIndexExists(personIndex + basePersonIndex);
			}

			boolean groupCreationSubscribersExist = dataManagerContext.subscribersExist(GroupAdditionEvent.class);

			List<GroupId> newGroups = new ArrayList<>();

			for (int i = 0; i < groupCount; i++) {
				GroupTypeId groupTypeId = bulkGroupMembershipData.getGroupTypeId(i);

				final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
				List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
				if (groups == null) {
					groups = new ArrayList<>();
					typesToGroupsMap.setValue(typeIndex, groups);
				}
				GroupId groupId = new GroupId(masterGroupId++);

				groups.add(groupId);
				groupsToTypesMap.setIntValue(groupId.getValue(), typeIndex);

				newGroups.add(groupId);

				if (groupCreationSubscribersExist) {
					dataManagerContext.releaseEvent(new GroupAdditionEvent(groupId));
				}
			}

			for (Integer personIndex : bulkGroupMembershipData.getPersonIndices()) {
				PersonId boxedPersonId = personDataManager.getBoxedPersonId(personIndex + basePersonIndex).get();
				List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
				for (Integer groupIndex : groupIndices) {
					GroupId groupId = newGroups.get(groupIndex);
					validatePersonNotInGroup(boxedPersonId, groupId);

					List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
					if (people == null) {
						people = new ArrayList<>();
						groupsToPeopleMap.setValue(groupId.getValue(), people);
					}
					people.add(boxedPersonId);

					List<GroupId> groups = peopleToGroupsMap.getValue(boxedPersonId.getValue());
					if (groups == null) {
						groups = new ArrayList<>(1);
						peopleToGroupsMap.setValue(boxedPersonId.getValue(), groups);
					}
					groups.add(groupId);
				}
			}

		}
	}

	private void handlePersonImminentRemovalEvent(final DataManagerContext dataManagerContext, PersonImminentRemovalEvent personImminentRemovalEvent) {
		validatePersonExists(personImminentRemovalEvent.getPersonId());
		dataManagerContext.addPlan((context) -> {
			removePerson(personImminentRemovalEvent.getPersonId());
		}, dataManagerContext.getTime());
	}

	/**
	 * Removes the person from all group tracking.
	 * 
	 */
	private void removePerson(final PersonId personId) {
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
}
