package plugins.groups.resolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.ResolverContext;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.groups.GroupPlugin;
import plugins.groups.datacontainers.PersonGroupDataManager;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupConstructionEvent;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupMembershipRemovalEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.events.observation.GroupCreationObservationEvent;
import plugins.groups.events.observation.GroupImminentRemovalObservationEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.events.observation.GroupPropertyChangeObservationEvent;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.BulkGroupMembershipData;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain GroupPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain PersonGroupDataView}.
 * Initializes this data views from the {@linkplain GroupInitialData} instance
 * provided to the plugin.
 * </P>
 * 
 * 
 * <P>
 * Initializes all event labelers defined by
 * <ul>
 * <li>{@linkplain GroupMembershipAdditionObservationEvent}</li>
 * <li>{@linkplain GroupMembershipRemovalObservationEvent}</li>
 * <li>{@linkplain GroupCreationObservationEvent}</li>
 * <li>{@linkplain GroupImminentRemovalObservationEvent}</li>
 * <li>{@linkplain GroupPropertyChangeObservationEvent}</li>
 * </ul>
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain BulkPersonCreationObservationEvent} <blockquote>Assigns the
 * newly created people into newly created groups on the basis of auxiliary data
 * carried in the event as a BulkGroupMembershipData. Publishes the groups to
 * the person group data view. Generates the corresponding
 * {@linkplain GroupCreationObservationEvent} events. <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the event contains an unknown person id</li> 
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the BulkMembership data exists
 * and contains an unknown person id, i.e. it uses a person index</li>
 * <li>{@link GroupError#UNKNOWN_GROUP_TYPE_ID} if the BulkMembership data
 * exists and contains an unknown group type id</li>
 *
 * 
 * </ul>
 * </blockquote></li>
 * 
 * <li>{@linkplain GroupCreationEvent} <blockquote> Adds a group. Publishes the
 * group membership change to the person group data view. Generates the
 * corresponding {@linkplain GroupCreationObservationEvent} event. <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link GroupError#NULL_GROUP_TYPE_ID} if the group type id is null</li>
 * <li>{@link GroupError#UNKNOWN_GROUP_TYPE_ID} if the group type id is
 * unknown</li>
 * </ul>
 * </blockquote></li>
 * 
 * 
 * <li>{@linkplain GroupMembershipAdditionEvent} <blockquote>Adds a person to a
 * group. Publishes the group membership change to the person group data view.
 * Generates the corresponding
 * {@linkplain GroupMembershipAdditionObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
 * <li>{@link GroupError#NULL_GROUP_ID} if the group id is null</li>
 * <li>{@link GroupError#UNKNOWN_GROUP_ID} if the group id is
 * unknown</li>
 * <li>{@link GroupError#DUPLICATE_GROUP_MEMBERSHIP} if the person is already a
 * member of the group</li>
 * </ul>
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain GroupMembershipRemovalEvent} <blockquote> Removes a person
 * from a group. Publishes the group membership change to the person group data
 * view. Generates the corresponding
 * {@linkplain GroupMembershipRemovalObservationEvent} event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
 * <li>{@link GroupError#NULL_GROUP_ID} if the group id is null</li>
 * <li>{@link GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the group id is
 * unknown</li>
 * <li>{@link GroupError#NON_GROUP_MEMBERSHIP} if the person is not a member of
 * the group</li>
 * </ul>
 * 
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain GroupPropertyValueAssignmentEvent}<blockquote> Sets a
 * property value for a group. Publishes the group property change to the person
 * group data view. Generates the corresponding
 * {@linkplain GroupPropertyChangeObservationEvent} event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain GroupError.NULL_GROUP_ID } if the group id is null</li>
 * <li>{@linkplain GroupError.UNKNOWN_GROUP_ID } if the group id is unknown</li>
 * <li>{@linkplain GroupError.NULL_GROUP_PROPERTY_ID } if the group property id
 * is null</li>
 * <li>{@linkplain GroupError.UNKNOWN_GROUP_PROPERTY_ID } if the group property
 * id is unknown</li>
 * <li>{@linkplain PropertyError.IMMUTABLE_VALUE } if the corresponding property
 * definition defines the property as immutable</li>
 * <li>{@linkplain GroupError.NULL_GROUP_PROPERTY_VALUE } if the property value
 * is null</li>
 * <li>{@linkplain PropertyError.INCOMPATIBLE_VALUE } if property value is
 * incompatible with the corresponding property definition</li>
 * </ul>
 * 
 * </blockquote></li>
 *
 * 
 * <li>{@linkplain GroupRemovalRequestEvent}<blockquote> Removes the group.
 * Publishes the group removal to the person group data view. Generates the
 * corresponding {@linkplain GroupImminentRemovalObservationEvent} event. <BR>
 * <BR>
 * Throws {@linkplain ContractException}
 * <ul>
 * <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is null</li>
 * <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id is unknown</li>
 * </ul>
 * 
 * </blockquote></li>
 * 
 * 
 * <li>{@linkplain PersonImminentRemovalObservationEvent}<blockquote> * Removes
 * the person from all groups by scheduling the removal for the current time.
 * This allows references and group memberships to remain long enough for
 * resolvers, agents and reports to have final reference to the person while
 * still associated with any relevant groups. {@linkplain CompartmentDataView}
 * <BR>
 * <BR>
 * Throws {@linkplain ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
 * unknown</li>
 * </ul>
 * 
 * </blockquote></li>
 * </ul>
 * 
 * <li>{@linkplain GroupConstructionEvent}<blockquote> Adds a group with any
 * group property initialization that is contained in the events's auxiliary
 * data. Publishes the group membership change to the person group data view.
 * Generates the corresponding {@linkplain GroupCreationObservationEvent} event.
 * <BR>
 * <BR>
 * Throws {@linkplain ContractException}
 * <ul>
 * <li>{@linkplain GroupError#NULL_GROUP_CONSTRUCTION_INFO} if the group
 * construction info is null</li>
 * <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the group type id
 * contained in the group construction info is unknown</li>
 * <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if a group property id
 * contained in the group construction info is unknown</li>
 * <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a group property value
 * contained in the group construction info is incompatible with the
 * corresponding property definition.</li>
 * 
 * 
 * </ul>
 * 
 * </blockquote></li>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */

public final class GroupEventResolver {
	private GroupInitialData groupInitialData;

	/**
	 * Constructs the resolver.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_INITIALIZATION_DATA}</li>
	 */
	public GroupEventResolver(GroupInitialData groupInitialData) {
		if (groupInitialData == null) {
			throw new ContractException(GroupError.NULL_GROUP_INITIALIZATION_DATA);
		}
		this.groupInitialData = groupInitialData;
	}

	private PersonDataView personDataView;

	private PersonGroupDataManager personGroupDataManager;

	private void handleGroupConstructionEventValidation(final ResolverContext resolverContext, final GroupConstructionEvent groupConstructionEvent) {
		final GroupConstructionInfo groupConstructionInfo = groupConstructionEvent.getGroupConstructionInfo();
		validateGroupConstructionInfoNotNull(resolverContext, groupConstructionInfo);
		final GroupTypeId groupTypeId = groupConstructionInfo.getGroupTypeId();
		validateGroupTypeId(resolverContext, groupConstructionInfo.getGroupTypeId());

		final Map<GroupPropertyId, Object> propertyValues = groupConstructionInfo.getPropertyValues();
		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			validateGroupPropertyId(resolverContext, groupTypeId, groupPropertyId);
			final PropertyDefinition propertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			validateGroupPropertyValueNotNull(resolverContext, groupPropertyValue);
			validateValueCompatibility(resolverContext, groupPropertyId, propertyDefinition, groupPropertyValue);
		}
	}

	private void handleGroupConstructionEventExecution(final ResolverContext resolverContext, final GroupConstructionEvent groupConstructionEvent) {
		final GroupConstructionInfo groupConstructionInfo = groupConstructionEvent.getGroupConstructionInfo();
		final GroupTypeId groupTypeId = groupConstructionInfo.getGroupTypeId();
		final Map<GroupPropertyId, Object> propertyValues = groupConstructionInfo.getPropertyValues();
		GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
		for (final GroupPropertyId groupPropertyId : propertyValues.keySet()) {
			final Object groupPropertyValue = propertyValues.get(groupPropertyId);
			personGroupDataManager.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
		}
		resolverContext.queueEventForResolution(new GroupCreationObservationEvent(groupId));
	}

	private void handleGroupCreationEventValidation(final ResolverContext resolverContext, final GroupCreationEvent groupCreationEvent) {
		final GroupTypeId groupTypeId = groupCreationEvent.getGroupTypeId();
		validateGroupTypeId(resolverContext, groupTypeId);
	}

	private void handleGroupCreationEventExecution(final ResolverContext resolverContext, final GroupCreationEvent groupCreationEvent) {
		final GroupTypeId groupTypeId = groupCreationEvent.getGroupTypeId();
		GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
		resolverContext.queueEventForResolution(new GroupCreationObservationEvent(groupId));
	}

	private void handleBulkPersonCreationObservationEventValidation(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		Optional<BulkGroupMembershipData> optional = bulkPersonContructionData.getValue(BulkGroupMembershipData.class);
		if (optional.isPresent()) {
			int personCount = bulkPersonContructionData.getPersonContructionDatas().size();
			PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
			int basePersonIndex = personId.getValue();
			for (int i = 0; i < personCount; i++) {
				validatePersonIndexExists(resolverContext, i + basePersonIndex);
			}
			BulkGroupMembershipData bulkGroupMembershipData = optional.get();
			int groupCount = bulkGroupMembershipData.getGroupCount();
			for (int i = 0; i < groupCount; i++) {
				GroupTypeId groupTypeId = bulkGroupMembershipData.getGroupTypeId(i);
				validateGroupTypeId(resolverContext, groupTypeId);
			}
			for (Integer personIndex : bulkGroupMembershipData.getPersonIndices()) {
				validatePersonIndexExists(resolverContext, personIndex + basePersonIndex);
			}
			
			

		}
	}

	private void handleBulkPersonCreationObservationEventExecution(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {

		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		Optional<BulkGroupMembershipData> optional = bulkPersonContructionData.getValue(BulkGroupMembershipData.class);
		if (optional.isPresent()) {
			PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
			int basePersonIndex = personId.getValue();
			BulkGroupMembershipData bulkGroupMembershipData = optional.get();
			int groupCount = bulkGroupMembershipData.getGroupCount();

			
			boolean groupCreationSubscribersExist = resolverContext.subscribersExistForEvent(GroupCreationObservationEvent.class);

			List<GroupId> groupIds = new ArrayList<>();

			for (int i = 0; i < groupCount; i++) {
				GroupTypeId groupTypeId = bulkGroupMembershipData.getGroupTypeId(i);
				GroupId groupId = personGroupDataManager.addGroup(groupTypeId);
				groupIds.add(groupId);

				if (groupCreationSubscribersExist) {
					resolverContext.queueEventForResolution(new GroupCreationObservationEvent(groupId));
				}
			}

			for (Integer personIndex : bulkGroupMembershipData.getPersonIndices()) {
				PersonId boxedPersonId = personDataView.getBoxedPersonId(personIndex + basePersonIndex);
				List<Integer> groupIndices = bulkGroupMembershipData.getGroupIndicesForPersonIndex(personIndex);
				for (Integer groupIndex : groupIndices) {
					GroupId groupId = groupIds.get(groupIndex);
					validatePersonNotInGroup(resolverContext, boxedPersonId, groupId);
					personGroupDataManager.addPersonToGroup(groupId, boxedPersonId);					
				}
			}

		}
	}

	private void handleGroupMembershipAdditionEventValidation(final ResolverContext resolverContext, final GroupMembershipAdditionEvent groupMembershipAdditionEvent) {
		final PersonId personId = groupMembershipAdditionEvent.getPersonId();
		final GroupId groupId = groupMembershipAdditionEvent.getGroupId();
		validatePersonExists(resolverContext, personId);
		validateGroupExists(resolverContext, groupId);
		validatePersonNotInGroup(resolverContext, personId, groupId);
	}

	private void handleGroupMembershipAdditionEventExecution(final ResolverContext resolverContext, final GroupMembershipAdditionEvent groupMembershipAdditionEvent) {
		final PersonId personId = groupMembershipAdditionEvent.getPersonId();
		final GroupId groupId = groupMembershipAdditionEvent.getGroupId();
		personGroupDataManager.addPersonToGroup(groupId, personId);
		resolverContext.queueEventForResolution(new GroupMembershipAdditionObservationEvent(personId, groupId));
	}

	private void handlePersonImminentRemovalObservationEventValidation(final ResolverContext resolverContext, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(resolverContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final ResolverContext resolverContext, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> personGroupDataManager.removePerson(personImminentRemovalObservationEvent.getPersonId()), resolverContext.getTime());
	}

	/**
	 * Initial behavior of this resolver.
	 * 
	 * <li>Adds all event labelers defined by the following events <blockquote>
	 * <ul>
	 * <li>{@linkplain GroupMembershipAdditionObservationEvent}</li>
	 * <li>{@linkplain GroupMembershipRemovalObservationEvent}</li>
	 * <li>{@linkplain GroupCreationObservationEvent}</li>
	 * <li>{@linkplain GroupImminentRemovalObservationEvent}</li>
	 * <li>{@linkplain GroupPropertyChangeObservationEvent}</li>
	 * </ul>
	 * </blockquote></li>
	 * 
	 *
	 * <li>Subscribes to all handled events *
	 * 
	 * <li>Adds groups, group memberships, group properties from the
	 * {@linkplain GroupInitialData}</li>
	 * 
	 * <li>Publishes the {@linkplain PersonGroupDataView}</li>
	 */
	public void init(final ResolverContext resolverContext) {

		/*
		 * Subscribe to all the various events that can update group data --
		 * i.e. the mutation events in the groups plugin as well as person
		 * creation and removal events.
		 */
		resolverContext.subscribeToEventValidationPhase(GroupConstructionEvent.class, this::handleGroupConstructionEventValidation);
		resolverContext.subscribeToEventExecutionPhase(GroupConstructionEvent.class, this::handleGroupConstructionEventExecution);

		resolverContext.subscribeToEventExecutionPhase(GroupCreationEvent.class, this::handleGroupCreationEventExecution);
		resolverContext.subscribeToEventValidationPhase(GroupCreationEvent.class, this::handleGroupCreationEventValidation);

		resolverContext.subscribeToEventExecutionPhase(GroupMembershipAdditionEvent.class, this::handleGroupMembershipAdditionEventExecution);
		resolverContext.subscribeToEventValidationPhase(GroupMembershipAdditionEvent.class, this::handleGroupMembershipAdditionEventValidation);

		resolverContext.subscribeToEventExecutionPhase(GroupMembershipRemovalEvent.class, this::handleGroupMembershipRemovalEventExecution);
		resolverContext.subscribeToEventValidationPhase(GroupMembershipRemovalEvent.class, this::handleGroupMembershipRemovalEventValidation);

		resolverContext.subscribeToEventExecutionPhase(GroupPropertyValueAssignmentEvent.class, this::handleGroupPropertyValueAssignmentEventExecution);
		resolverContext.subscribeToEventValidationPhase(GroupPropertyValueAssignmentEvent.class, this::handleGroupPropertyValueAssignmentEventValidation);

		resolverContext.subscribeToEventExecutionPhase(GroupRemovalRequestEvent.class, this::handleGroupRemovalRequestEventExecution);
		resolverContext.subscribeToEventValidationPhase(GroupRemovalRequestEvent.class, this::handleGroupRemovalRequestEventValidation);

		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);
		resolverContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);
		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);

		
		/*
		 * Establish the person data view
		 */
		personDataView = resolverContext.getDataView(PersonDataView.class).get();

		/*
		 * Establish mappings from the ids used to create people and groups in
		 * the initial data onto those used in the simulation. The initial data
		 * has no requirement to use contiguous id values, but the simulation is
		 * more efficient if these ids are contiguous.
		 */

		final Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();
		final Map<GroupId, GroupId> scenarioToSimGroupMap = getScenarioToSimGroupMap(groupInitialData);

		/*
		 * Build the person group data manager and load it from the group
		 * initial data, utilizing the id mappings established above. The order
		 * of these load method invocations is sensitive but fairly obvious.
		 */
		personGroupDataManager = new PersonGroupDataManager(resolverContext.getSafeContext());
		loadGroupTypes(resolverContext, groupInitialData);
		loadGroupPropertyDefinitions(resolverContext, groupInitialData);
		loadGroups(resolverContext, groupInitialData, scenarioToSimGroupMap);
		loadGroupMembership(resolverContext, groupInitialData, scenarioToSimPeopleMap, scenarioToSimGroupMap);
		loadGroupPropertyValues(resolverContext, groupInitialData, scenarioToSimGroupMap);

		/*
		 * Publish the person group data view
		 */
		PersonGroupDataView personGroupDataView = new PersonGroupDataView(resolverContext.getSafeContext(), personGroupDataManager);
		resolverContext.publishDataView(personGroupDataView);

		
		/*
		 * Add the event labelers associated with the various observation events
		 * contained in the groups plugin.
		 */
		
		
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForGroup());
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForGroupAndPerson());
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForGroupType(personGroupDataView));
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForGroupTypeAndPerson(personGroupDataView));
		resolverContext.addEventLabeler(GroupMembershipAdditionObservationEvent.getEventLabelerForPerson());

		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForGroup());
		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForGroupAndPerson());
		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForGroupType(personGroupDataView));
		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForGroupTypeAndPerson(personGroupDataView));
		resolverContext.addEventLabeler(GroupMembershipRemovalObservationEvent.getEventLabelerForPerson());

		resolverContext.addEventLabeler(GroupCreationObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(GroupCreationObservationEvent.getEventLabelerForGroupType(personGroupDataView));

		resolverContext.addEventLabeler(GroupImminentRemovalObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(GroupImminentRemovalObservationEvent.getEventLabelerForGroup());
		resolverContext.addEventLabeler(GroupImminentRemovalObservationEvent.getEventLabelerForGroupType(personGroupDataView));

		resolverContext.addEventLabeler(GroupPropertyChangeObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(GroupPropertyChangeObservationEvent.getEventLabelerForGroup());
		resolverContext.addEventLabeler(GroupPropertyChangeObservationEvent.getEventLabelerForGroupAndProperty());
		resolverContext.addEventLabeler(GroupPropertyChangeObservationEvent.getEventLabelerForGroupType(personGroupDataView));
		resolverContext.addEventLabeler(GroupPropertyChangeObservationEvent.getEventLabelerForGroupTypeAndProperty(personGroupDataView));

		
		/*
		 * Release reference to the initial data to allow for early garbage
		 * collection
		 */
		groupInitialData = null;
	}

	private void loadGroupPropertyDefinitions(ResolverContext resolverContext, GroupInitialData groupInitialData) {
		for (final GroupTypeId groupTypeId : groupInitialData.getGroupTypeIds()) {
			final Set<GroupPropertyId> propertyIds = groupInitialData.getGroupPropertyIds(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIds) {
				final PropertyDefinition propertyDefinition = groupInitialData.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				personGroupDataManager.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
			}
		}

	}

	private void loadGroupTypes(ResolverContext resolverContext, GroupInitialData groupInitialData) {
		final Set<GroupTypeId> groupTypeIds = groupInitialData.getGroupTypeIds();

		for (final GroupTypeId groupTypeId : groupTypeIds) {
			personGroupDataManager.addGroupType(groupTypeId);
		}
	}

	private Map<GroupId, GroupId> getScenarioToSimGroupMap(GroupInitialData groupInitialData) {
		final List<GroupId> scenarioGroupIds = new ArrayList<>(groupInitialData.getGroupIds());
		Collections.sort(scenarioGroupIds);
		final Map<GroupId, GroupId> result = new LinkedHashMap<>();
		int id = 0;
		for (final GroupId scenarioGroupId : scenarioGroupIds) {
			final GroupId simulationGroupId = new GroupId(id++);
			result.put(scenarioGroupId, simulationGroupId);
		}
		return result;
	}

	private void loadGroupMembership(final ResolverContext resolverContext, final GroupInitialData groupInitialData, final Map<PersonId, PersonId> scenarioToSimPeopleMap,
			final Map<GroupId, GroupId> scenarioToSimGroupMap) {
		for (final GroupId scenarioGroupId : groupInitialData.getGroupIds()) {
			final Set<PersonId> scenarioGroupMembers = groupInitialData.getGroupMembers(scenarioGroupId);
			final GroupId simulationGroupId = scenarioToSimGroupMap.get(scenarioGroupId);
			for (final PersonId scenarioPersonId : scenarioGroupMembers) {
				final PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
				personGroupDataManager.addPersonToGroup(simulationGroupId, simulationPersonId);
			}
		}
	}

	private void loadGroupPropertyValues(final ResolverContext resolverContext, final GroupInitialData groupInitialData, final Map<GroupId, GroupId> scenarioToSimGroupMap) {
		for (final GroupId scenarioGroupId : groupInitialData.getGroupIds()) {
			final GroupTypeId groupTypeId = groupInitialData.getGroupTypeId(scenarioGroupId);
			for (final GroupPropertyId groupPropertyId : groupInitialData.getGroupPropertyIds(groupTypeId)) {
				final Object groupPropertyValue = groupInitialData.getGroupPropertyValue(scenarioGroupId, groupPropertyId);
				final PropertyDefinition propertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				Object defaultValue = propertyDefinition.getDefaultValue().get();
				if (!groupPropertyValue.equals(defaultValue)) {
					final GroupId simulationGroupId = scenarioToSimGroupMap.get(scenarioGroupId);
					personGroupDataManager.setGroupPropertyValue(simulationGroupId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}

	private void loadGroups(final ResolverContext resolverContext, final GroupInitialData groupInitialData, Map<GroupId, GroupId> scenarioToSimGroupMap) {
		/*
		 * Build the map that will translate the group ids recorded in the
		 * scenario into a contiguous set of integers starting with zero.
		 */

		final List<GroupId> scenarioGroupIds = new ArrayList<>(groupInitialData.getGroupIds());
		Collections.sort(scenarioGroupIds);

		for (final GroupId scenarioGroupId : scenarioGroupIds) {
			final GroupTypeId groupTypeId = groupInitialData.getGroupTypeId(scenarioGroupId);
			validateGroupTypeId(resolverContext, groupTypeId);
			final GroupId simulationGroupId = personGroupDataManager.addGroup(groupTypeId);
			GroupId expectedSimulationGroupId = scenarioToSimGroupMap.get(scenarioGroupId);
			if (expectedSimulationGroupId == null) {
				throw new RuntimeException("null expected simulation group id");
			}
			if (!expectedSimulationGroupId.equals(simulationGroupId)) {
				throw new RuntimeException("expected simulation group id," + expectedSimulationGroupId + ", does not match actual group id " + simulationGroupId);
			}
		}
	}

	private void handleGroupRemovalRequestEventValidation(final ResolverContext resolverContext, final GroupRemovalRequestEvent groupRemovalRequestEvent) {
		final GroupId groupId = groupRemovalRequestEvent.getGroupId();
		validateGroupExists(resolverContext, groupId);
	}

	private void handleGroupRemovalRequestEventExecution(final ResolverContext resolverContext, final GroupRemovalRequestEvent groupRemovalRequestEvent) {
		final GroupId groupId = groupRemovalRequestEvent.getGroupId();
		resolverContext.queueEventForResolution(new GroupImminentRemovalObservationEvent(groupId));
		resolverContext.addPlan((context) -> personGroupDataManager.removeGroup(groupRemovalRequestEvent.getGroupId()), resolverContext.getTime());
	}

	private void handleGroupMembershipRemovalEventExecution(final ResolverContext resolverContext, final GroupMembershipRemovalEvent groupMembershipRemovalEvent) {
		final GroupId groupId = groupMembershipRemovalEvent.getGroupId();
		final PersonId personId = groupMembershipRemovalEvent.getPersonId();
		personGroupDataManager.removePersonFromGroup(groupId, personId);
		resolverContext.queueEventForResolution(new GroupMembershipRemovalObservationEvent(personId, groupId));
	}

	private void handleGroupMembershipRemovalEventValidation(final ResolverContext resolverContext, final GroupMembershipRemovalEvent groupMembershipRemovalEvent) {
		final GroupId groupId = groupMembershipRemovalEvent.getGroupId();
		final PersonId personId = groupMembershipRemovalEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		validateGroupExists(resolverContext, groupId);
		validatePersonInGroup(resolverContext, personId, groupId);
	}

	private void handleGroupPropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent) {
		final GroupId groupId = groupPropertyValueAssignmentEvent.getGroupId();
		final GroupPropertyId groupPropertyId = groupPropertyValueAssignmentEvent.getGroupPropertyId();
		final Object groupPropertyValue = groupPropertyValueAssignmentEvent.getGroupPropertyValue();

		Object oldValue = personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
		personGroupDataManager.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
		resolverContext.queueEventForResolution(new GroupPropertyChangeObservationEvent(groupId, groupPropertyId, oldValue, groupPropertyValue));
	}

	private void handleGroupPropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent) {
		final GroupId groupId = groupPropertyValueAssignmentEvent.getGroupId();
		final GroupPropertyId groupPropertyId = groupPropertyValueAssignmentEvent.getGroupPropertyId();
		final Object groupPropertyValue = groupPropertyValueAssignmentEvent.getGroupPropertyValue();

		validateGroupExists(resolverContext, groupId);
		final GroupTypeId groupTypeId = personGroupDataManager.getGroupType(groupId);
		validateGroupPropertyId(resolverContext, groupTypeId, groupPropertyId);
		final PropertyDefinition propertyDefinition = personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
		validatePropertyMutability(resolverContext, propertyDefinition);
		validateGroupPropertyValueNotNull(resolverContext, groupPropertyValue);
		validateValueCompatibility(resolverContext, groupPropertyId, propertyDefinition, groupPropertyValue);
	}

	/**
	 * Validates the group type id
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 */
	private void validateGroupConstructionInfoNotNull(final ResolverContext resolverContext, final GroupConstructionInfo groupConstructionInfo) {
		if (groupConstructionInfo == null) {
			resolverContext.throwContractException(GroupError.NULL_GROUP_CONSTRUCTION_INFO);
		}

	}

	/**
	 * Validates that the group exists
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group does
	 *             not exist
	 *
	 *
	 */
	private void validateGroupExists(final ResolverContext resolverContext, final GroupId groupId) {
		if (groupId == null) {
			resolverContext.throwContractException(GroupError.NULL_GROUP_ID);
		}
		if (!personGroupDataManager.groupExists(groupId)) {
			resolverContext.throwContractException(GroupError.UNKNOWN_GROUP_ID);
		}
	}

	private void validateGroupPropertyId(final ResolverContext resolverContext, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			resolverContext.throwContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
		if (!personGroupDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			resolverContext.throwContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID);
		}
	}

	private void validateGroupPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
		}
	}

	/**
	 * Validates the group type id
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 */
	private void validateGroupTypeId(final ResolverContext resolverContext, final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			resolverContext.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (!this.personGroupDataManager.groupTypeIdExists(groupTypeId)) {
			resolverContext.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}

	}

	private void validatePersonExists(final ResolverContext resolverContext, final PersonId personId) {
		if (personId == null) {
			resolverContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonIndexExists(final ResolverContext resolverContext, final int personIndex) {
		if (!personDataView.personIndexExists(personIndex)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Preconditions : the person and group exist
	 */
	private void validatePersonInGroup(final ResolverContext resolverContext, final PersonId personId, final GroupId groupId) {
		if (!personGroupDataManager.isGroupMember(groupId, personId)) {
			resolverContext.throwContractException(GroupError.NON_GROUP_MEMBERSHIP, "Person " + personId + " is not a member of group " + groupId);
		}
	}

	/*
	 * Preconditions : the person and group exist
	 */
	private void validatePersonNotInGroup(final ResolverContext resolverContext, final PersonId personId, final GroupId groupId) {
		if (personGroupDataManager.isGroupMember(groupId, personId)) {
			resolverContext.throwContractException(GroupError.DUPLICATE_GROUP_MEMBERSHIP, "Person " + personId + " is already a member of group " + groupId);
		}
	}

	private void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

}
