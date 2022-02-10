package plugins.groups.datacontainers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import net.jcip.annotations.GuardedBy;
import nucleus.SimulationContext;
import nucleus.NucleusError;
import plugins.compartments.CompartmentPlugin;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.people.support.PersonId;
import plugins.properties.support.BooleanPropertyManager;
import plugins.properties.support.DoublePropertyManager;
import plugins.properties.support.EnumPropertyManager;
import plugins.properties.support.FloatPropertyManager;
import plugins.properties.support.IndexedPropertyManager;
import plugins.properties.support.IntPropertyManager;
import plugins.properties.support.ObjectPropertyManager;
import plugins.properties.support.PropertyDefinition;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.support.RandomNumberGeneratorId;
import util.ContractException;
import util.arraycontainers.IntValueContainer;
import util.arraycontainers.ObjectValueContainer;

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

public final class PersonGroupDataManager {

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

	private GroupId lastIssuedGroupId;

	private final StochasticsDataManager stochasticsDataManager;

	private final SimulationContext simulationContext;

	/**
	 * Add the group type to this manager. Group types must be added prior to
	 * groups and group properties.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type is null</li>
	 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_TYPE} if the group
	 *             type was previously added</li>
	 */
	public void addGroupType(GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			simulationContext.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		if (typesToIndexesMap.keySet().contains(groupTypeId)) {
			simulationContext.throwContractException(GroupError.DUPLICATE_GROUP_TYPE);
		}
		final int index = typesToIndexesMap.size();
		typesToIndexesMap.put(groupTypeId, index);
		indexesToTypesMap.add(groupTypeId);

		groupPropertyManagerMap.put(groupTypeId, new LinkedHashMap<>());
		groupPropertyDefinitions.put(groupTypeId, new LinkedHashMap<>());
	}

	/**
	 * Adds the group property to this manager
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#DUPLICATE_GROUP_PROPERTY_DEFINITION}
	 *             if the group property is already defined</li>
	 *             <li>{@linkplain GroupError#NULL_PROPERTY_DEFINITION} if the
	 *             property definition is null</li>
	 */
	public void defineGroupProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId, PropertyDefinition propertyDefinition) {

		if (groupTypeId == null) {
			simulationContext.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		if (!groupTypeIdExists(groupTypeId)) {
			simulationContext.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}

		if (groupPropertyId == null) {
			simulationContext.throwContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}

		if (getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			simulationContext.throwContractException(GroupError.DUPLICATE_GROUP_PROPERTY_DEFINITION);
		}

		if (propertyDefinition == null) {
			simulationContext.throwContractException(GroupError.NULL_PROPERTY_DEFINITION);
		}

		Map<GroupPropertyId, IndexedPropertyManager> managerMap = groupPropertyManagerMap.get(groupTypeId);
		Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);

		final IndexedPropertyManager indexedPropertyManager = getIndexedPropertyManager(simulationContext, propertyDefinition, 0);
		managerMap.put(groupPropertyId, indexedPropertyManager);
		map.put(groupPropertyId, propertyDefinition);

	}

	/**
	 * Constructs this person group data manager
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT}</li>
	 */
	public PersonGroupDataManager(final SimulationContext simulationContext) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		this.simulationContext = simulationContext;

		stochasticsDataManager = simulationContext.getDataView(StochasticsDataManager.class).get();
	}

	/**
	 * Returns the group id for a newly added group. The group is initialized as
	 * having the given type, no members and default property values.
	 * 
	 * Precondition : The group type id must be valid.
	 */
	public GroupId addGroup(final GroupTypeId groupTypeId) {
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups == null) {
			groups = new ArrayList<>();
			typesToGroupsMap.setValue(typeIndex, groups);
		}
		final GroupId result = new GroupId(masterGroupId++);
		lastIssuedGroupId = result;
		groups.add(result);
		groupsToTypesMap.setIntValue(result.getValue(), typeIndex);
		return result;
	}

	/**
	 * Associates the person with the group.
	 * 
	 * Preconditions : The group id must be valid. The person id must be valid.
	 * The person must not already be a member of the group.
	 */
	public void addPersonToGroup(final GroupId groupId, final PersonId personId) {

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
			simulationContext.throwContractException(NucleusError.ACCESS_VIOLATION, "cannot access weighted sampling during the execution of a previous weighted sampling");
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
	 * Returns the number of groups that have the given group type id.
	 * 
	 * Precondition : the group type id must be valid
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {
		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	/**
	 * Returns the number of groups that have the given group type for the given
	 * person.
	 * 
	 * Preconditions : the group type id must be valid and the person must
	 * exist.
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
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

	/**
	 * Returns the number of groups that contain the person
	 * 
	 * Preconditions : The person must exist;
	 */
	public int getGroupCountForPerson(final PersonId personId) {
		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return groups.size();
		}
		return 0;
	}

	/**
	 * Returns the groups in this manager in list form with no null entries or
	 * duplicates.
	 */
	public List<GroupId> getGroupIds() {
		final List<GroupId> result = new ArrayList<>();
		for (final GroupTypeId groupTypeId : typesToIndexesMap.keySet()) {
			result.addAll(getGroupsForGroupType(groupTypeId));
		}
		return result;
	}

	/**
	 * Returns the property definition associated with the given group type and
	 * group property id.
	 * 
	 * Preconditions : the property definition must exits, i.e. the group type
	 * and property id must exist and be associated with one another.
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		final Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		return map.get(groupPropertyId);
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
	 * Returns the group property ids for the given group type id
	 * 
	 * Preconditions: The group type id must be valid
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
		final Map<GroupPropertyId, PropertyDefinition> map = groupPropertyDefinitions.get(groupTypeId);
		final Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (final GroupPropertyId groupPropertyId : map.keySet()) {
			result.add((T) groupPropertyId);
		}
		return result;
	}

	/**
	 * Returns the time when the group property value was last set. Returns 0 if
	 * the value was only initialized.
	 * 
	 * Preconditions : The group and group property id must be valid and the
	 * group property id must be associated with the group's group type. The
	 * corresponding property definition must be defined with time tracking on.
	 */
	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		final GroupTypeId groupTypeId = getGroupType(groupId);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		return indexedPropertyManager.getPropertyTime(groupId.getValue());
	}

	/**
	 * Returns the value of the group's property.
	 * 
	 * Preconditions: The group and group property id must be valid and the
	 * group property id must be associated with the group's group type.
	 */
	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		final GroupTypeId groupTypeId = getGroupType(groupId);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		return indexedPropertyManager.getPropertyValue(groupId.getValue());
	}

	/**
	 * Returns the set groups associated with the given group type as a list.
	 * 
	 * Preconditions: The group type id must be valid
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {

		final Integer typeIndex = typesToIndexesMap.get(groupTypeId);
		final List<GroupId> groups = typesToGroupsMap.getValue(typeIndex);
		if (groups != null) {
			return new ArrayList<>(groups);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the set of groups containing the person as a list. Each such
	 * group will have the associated group type.
	 * 
	 * Preconditions: The group type id and person id must be a valid id of an
	 * existing.
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
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
	 * Returns the set of groups for the given person as a list.
	 * 
	 * Preconditions: The person id must be non-null
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId) {

		final List<GroupId> groups = peopleToGroupsMap.getValue(personId.getValue());
		if (groups != null) {
			return new ArrayList<>(groups);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the group type of the given group
	 * 
	 * Preconditions: The group id must be valid id of an existing group
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		return (T) indexesToTypesMap.get(groupsToTypesMap.getValueAsInt(groupId.getValue()));
	}

	/**
	 * Return the number of groups containing the given person
	 * 
	 * Preconditions: The person id must be a valid id of an existing person.
	 */
	public int getGroupTypeCountForPersonId(final PersonId personId) {

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
	 * Returns the group type ids.
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
	 * Returns the set of group types for the given person as a list.
	 * 
	 * Preconditions: The person id must be a valid id of an existing person.
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {

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
	 * Returns the last issued group id
	 */
	public Optional<GroupId> getLastIssuedGroupId() {
		return Optional.ofNullable(lastIssuedGroupId);
	}

	/**
	 * Returns the set of people for the given group as a list.
	 * 
	 * Preconditions: The group id must be a valid id of an existing group.
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {

		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return new ArrayList<>(people);
		}
		return new ArrayList<>();
	}

	/**
	 * Returns the set of people as a list for the given group type.
	 * 
	 * Preconditions: The group type id must be a valid.
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {

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
	 * Returns the number of people contained in the given group.
	 * 
	 * Preconditions: The group id must be a valid id for an existing group.
	 */
	public int getPersonCountForGroup(final GroupId groupId) {

		final List<PersonId> people = groupsToPeopleMap.getValue(groupId.getValue());
		if (people != null) {
			return people.size();
		}
		return 0;
	}

	/**
	 * Returns the number of people contained in groups of the given group type.
	 * 
	 * Preconditions: The group type id must be a valid.
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {

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
	 * Returns true if and only if person is a member of the group.
	 * 
	 * Preconditions: The person id must be a valid id for an existing person.
	 */
	public boolean isGroupMember(final GroupId groupId, final PersonId personId) {
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
	 * Removes the group from group management.
	 * 
	 * Preconditions: The group be a valid id of an existing group.
	 */
	public void removeGroup(final GroupId groupId) {

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

	}

	/**
	 * Removes the person from all group tracking.
	 *
	 * Preconditions : The person id must be a valid id of an existing (i.e.
	 * contained in this manager) person.
	 */
	public void removePerson(final PersonId personId) {
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

	/**
	 * Removes the person from the group, if they are a member
	 * 
	 * Preconditions: The person id must be a valid id of an existing person.
	 * The group id must be valid id of an existing group.
	 */
	public void removePersonFromGroup(final GroupId groupId, final PersonId personId) {

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

	}

	/**
	 * Returns a randomly selected person from the group using the group sampler
	 * to determine the probability for each person's selection. Returns an
	 * empty optional if no person meets the requirements of the group sampler.
	 * 
	 * Preconditions: The group id must be a valid id for an existing group. The
	 * excluded person, weighting function and random generator id contained in
	 * the group sampler must either be null or valid.
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId, final GroupSampler groupSampler) {

		RandomGenerator randomGenerator;
		if (groupSampler.getRandomNumberGeneratorId().isPresent()) {
			final RandomNumberGeneratorId randomNumberGeneratorId = groupSampler.getRandomNumberGeneratorId().get();
			randomGenerator = stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
		} else {
			randomGenerator = stochasticsDataManager.getRandomGenerator();
		}
		final GroupWeightingFunction groupWeightingFunction = groupSampler.getWeightingFunction().orElse(null);
		final PersonId excludedPersonId = groupSampler.getExcludedPerson().orElse(null);

		final boolean exclude = (excludedPersonId != null) && isGroupMember(groupId, excludedPersonId);
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
						final double weight = groupWeightingFunction.getWeight(simulationContext, personId, groupId);
						if (!Double.isFinite(weight) || (weight < 0)) {
							simulationContext.throwContractException(GroupError.MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION);

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
							simulationContext.throwContractException(GroupError.MALFORMED_GROUP_SAMPLE_WEIGHTING_FUNCTION);
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
	 * Sets a group's property value.
	 * 
	 * Preconditions: The group id must be a valid id for an existing group. The
	 * group property id must be valid and associated with the group's group
	 * type. The property value must be a non null value compatible with the
	 * associated property definition.F
	 */
	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		final GroupTypeId groupTypeId = getGroupType(groupId);
		final Map<GroupPropertyId, IndexedPropertyManager> map = groupPropertyManagerMap.get(groupTypeId);
		final IndexedPropertyManager indexedPropertyManager = map.get(groupPropertyId);
		indexedPropertyManager.setPropertyValue(groupId.getValue(), groupPropertyValue);
	}

}
