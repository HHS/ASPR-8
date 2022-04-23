package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * Event to indicating that person was removed from a group
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupMembershipRemovalEvent implements Event {
	private final PersonId personId;
	private final GroupId groupId;

	/**
	 * Constructs this event from the given person id and group id
	 * 
	 */
	public GroupMembershipRemovalEvent(final PersonId personId, final GroupId groupId) {
		super();
		this.personId = personId;
		this.groupId = groupId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

	/**
	 * Returns the person id used to create this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	private static void validateGroupId(SimulationContext simulationContext, GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
		GroupsDataManager personGroupDataView = simulationContext.getDataManager(GroupsDataManager.class);

		if (!personGroupDataView.groupExists(groupId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		PeopleDataManager peopleDataManager = simulationContext.getDataManager(PeopleDataManager.class);
		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validateGroupTypeId(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		GroupsDataManager groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		if (!groupsDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group id and
	 * person id.
	 *
	 * Preconditions : The context cannot be null
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
	public static EventLabel<GroupMembershipRemovalEvent> getEventLabelByGroupAndPerson(SimulationContext simulationContext, GroupId groupId, PersonId personId) {
		validateGroupId(simulationContext, groupId);
		validatePersonId(simulationContext, personId);
		return _getEventLabelByGroupAndPerson(groupId, personId);//
	}

	private static EventLabel<GroupMembershipRemovalEvent> _getEventLabelByGroupAndPerson(GroupId groupId, PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PERSON)//
							.addKey(GroupMembershipRemovalEvent.class)//
							.addKey(groupId)//
							.addKey(personId)//
							.build();//
	}

	
	/**
	 * Returns an event labeler for {@link GroupMembershipRemovalEvent} events
	 * that uses group id and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalEvent> getEventLabelerForGroupAndPerson() {
		return EventLabeler	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PERSON)//
							.setLabelFunction((context, event) -> _getEventLabelByGroupAndPerson(event.getGroupId(), event.getPersonId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 * 
	 */
	public static EventLabel<GroupMembershipRemovalEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return _getEventLabelByGroup(groupId);//
	}
	
	private static EventLabel<GroupMembershipRemovalEvent> _getEventLabelByGroup(GroupId groupId) {
		
		return EventLabel	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.addKey(GroupMembershipRemovalEvent.class)//
							.addKey(groupId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipRemovalEvent} events
	 * that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalEvent> getEventLabelerForGroup() {
		return EventLabeler	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.setLabelFunction((context, event) -> _getEventLabelByGroup(event.getGroupId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on person id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public static EventLabel<GroupMembershipRemovalEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return _getEventLabelByPerson(personId);
	}
	
	private static EventLabel<GroupMembershipRemovalEvent> _getEventLabelByPerson(PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.addKey(GroupMembershipRemovalEvent.class)//
							.addKey(personId)//
							.build();
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipRemovalEvent} events
	 * that uses person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalEvent> getEventLabelerForPerson() {

		return EventLabeler	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.setLabelFunction((context, event) -> _getEventLabelByPerson(event.getPersonId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on person id and
	 * group type id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 */
	public static EventLabel<GroupMembershipRemovalEvent> getEventLabelByGroupTypeAndPerson(SimulationContext simulationContext, GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validatePersonId(simulationContext, personId);
		return _getEventLabelByGroupTypeAndPerson(groupTypeId, personId);//
	}
	
	private static EventLabel<GroupMembershipRemovalEvent> _getEventLabelByGroupTypeAndPerson(GroupTypeId groupTypeId, PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipRemovalEvent.class).setEventLabelerId(LabelerId.TYPE_PERSON)//
							.addKey(GroupMembershipRemovalEvent.class)//
							.addKey(groupTypeId)//
							.addKey(personId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipRemovalEvent} events
	 * that uses group type id and person id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalEvent> getEventLabelerForGroupTypeAndPerson(GroupsDataManager groupsDataManager) {
		return EventLabeler	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.TYPE_PERSON)//
							.setLabelFunction((context, event) -> {
								GroupId groupId = event.getGroupId();
								GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
								return _getEventLabelByGroupTypeAndPerson(groupTypeId, event.getPersonId());
							})//
							.build();

	}

	private static enum LabelerId implements EventLabelerId {
		GROUP_PERSON, GROUP, PERSON, TYPE_PERSON, TYPE
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalEvent} events. Matches on group type id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 */
	public static EventLabel<GroupMembershipRemovalEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return _getEventLabelByGroupType(groupTypeId);//
	}
	
	private static EventLabel<GroupMembershipRemovalEvent> _getEventLabelByGroupType(GroupTypeId groupTypeId) {
		
		return EventLabel	.builder(GroupMembershipRemovalEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.addKey(GroupMembershipRemovalEvent.class)//
							.addKey(groupTypeId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipRemovalEvent} Matches
	 * on group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalEvent> getEventLabelerForGroupType(GroupsDataManager groupsDataManager) {
		return EventLabeler	.builder(GroupMembershipRemovalEvent.class).setEventLabelerId(LabelerId.TYPE)//
							.setLabelFunction((context, event) -> {
								GroupId groupId = event.getGroupId();
								GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
								return _getEventLabelByGroupType(groupTypeId);
							})//
							.build();
	}

}
