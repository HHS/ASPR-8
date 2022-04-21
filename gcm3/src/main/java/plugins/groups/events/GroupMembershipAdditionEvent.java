package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * Event to indicating that person was added to a group
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GroupMembershipAdditionEvent implements Event {
	private final PersonId personId;
	private final GroupId groupId;

	/**
	 * Constructs this event from the given person id and group id
	 * 
	 */
	public GroupMembershipAdditionEvent(final PersonId personId, final GroupId groupId) {
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

	private static enum LabelerId implements EventLabelerId {
		GROUP_PERSON, GROUP, PERSON, TYPE_PERSON, TYPE
	}

	private static void validateGroupId(SimulationContext simulationContext, GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();

		if (!groupDataManager.groupExists(groupId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataManager personDataManager = simulationContext.getDataManager(PersonDataManager.class).get();
		if (!personDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validateGroupTypeId(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		if (!groupDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group id and
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
	public static EventLabel<GroupMembershipAdditionEvent> getEventLabelByGroupAndPerson(SimulationContext simulationContext, GroupId groupId, PersonId personId) {
		validateGroupId(simulationContext, groupId);
		validatePersonId(simulationContext, personId);
		return _getEventLabelByGroupAndPerson(groupId, personId);//
	}
	
	private static EventLabel<GroupMembershipAdditionEvent> _getEventLabelByGroupAndPerson(GroupId groupId, PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PERSON)//
							.addKey(GroupMembershipAdditionEvent.class)//
							.addKey(groupId)//
							.addKey(personId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipAdditionEvent} events
	 * that uses group id and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionEvent> getEventLabelerForGroupAndPerson() {
		return EventLabeler	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PERSON)//
							.setLabelFunction((context, event) -> _getEventLabelByGroupAndPerson(event.getGroupId(), event.getPersonId()))//
							.build();//
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group id.
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
	public static EventLabel<GroupMembershipAdditionEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return _getEventLabelByGroup(groupId);//
	}
	
	
	private static EventLabel<GroupMembershipAdditionEvent> _getEventLabelByGroup(GroupId groupId) {
		
		return EventLabel	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.addKey(GroupMembershipAdditionEvent.class)//
							.addKey(groupId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipAdditionEvent} events
	 * that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionEvent> getEventLabelerForGroup() {
		return EventLabeler	.builder(GroupMembershipAdditionEvent.class).setEventLabelerId(LabelerId.GROUP)//
							.setLabelFunction((context, event) -> _getEventLabelByGroup(event.getGroupId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on person id.
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
	public static EventLabel<GroupMembershipAdditionEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return _getEventLabelByPerson(personId);

	}
	
	private static EventLabel<GroupMembershipAdditionEvent> _getEventLabelByPerson(PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.addKey(GroupMembershipAdditionEvent.class)//
							.addKey(personId)//
							.build();

	}

	/**
	 * Returns an event labeler for {@link GroupMembershipAdditionEvent} events
	 * that uses person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionEvent> getEventLabelerForPerson() {
		return EventLabeler	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.PERSON)//
							.setLabelFunction((context, event) -> _getEventLabelByPerson(event.getPersonId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on person id and
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
	public static EventLabel<GroupMembershipAdditionEvent> getEventLabelByGroupTypeAndPerson(SimulationContext simulationContext, GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validatePersonId(simulationContext, personId);
		return _getEventLabelByGroupTypeAndPerson(groupTypeId, personId);//
	}
	
	private static EventLabel<GroupMembershipAdditionEvent> _getEventLabelByGroupTypeAndPerson(GroupTypeId groupTypeId, PersonId personId) {
		
		return EventLabel	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE_PERSON)//
							.addKey(GroupMembershipAdditionEvent.class)//
							.addKey(groupTypeId)//
							.addKey(personId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipAdditionEvent} events
	 * that uses group type id and person id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionEvent> getEventLabelerForGroupTypeAndPerson(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE_PERSON)//
							.setLabelFunction((context, event) -> {
								GroupId groupId = event.getGroupId();
								GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
								return _getEventLabelByGroupTypeAndPerson(groupTypeId, event.getPersonId());
							})//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionEvent} events. Matches on group type id.
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
	public static EventLabel<GroupMembershipAdditionEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return _getEventLabelByGroupType(groupTypeId);//
	}
	
	private static EventLabel<GroupMembershipAdditionEvent> _getEventLabelByGroupType(GroupTypeId groupTypeId) {
		
		return EventLabel	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.addKey(GroupMembershipAdditionEvent.class)//
							.addKey(groupTypeId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupMembershipAdditionEvent} Matches
	 * on group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupMembershipAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.setLabelFunction((context, event) -> {
								GroupId groupId = event.getGroupId();
								GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
								return _getEventLabelByGroupType(groupTypeId);
							})//
							.build();
	}
	
}
