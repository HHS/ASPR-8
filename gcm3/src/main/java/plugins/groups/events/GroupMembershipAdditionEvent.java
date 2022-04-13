package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
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
public class GroupMembershipAdditionObservationEvent implements Event {
	private final PersonId personId;
	private final GroupId groupId;

	/**
	 * Constructs this event from the given person id and group id
	 * 
	 */
	public GroupMembershipAdditionObservationEvent(final PersonId personId, final GroupId groupId) {
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
		GROUP_PERSON, GROUP, PERSON, TYPE_PERSON, TYPE, ALL
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
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on group
	 * id and person id.
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupAndPerson(SimulationContext simulationContext, GroupId groupId, PersonId personId) {
		validateGroupId(simulationContext, groupId);
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.GROUP_PERSON, GroupMembershipAdditionObservationEvent.class, groupId, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} events that uses group id
	 * and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroupAndPerson() {
		return new SimpleEventLabeler<>(LabelerId.GROUP_PERSON, GroupMembershipAdditionObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.GROUP_PERSON, GroupMembershipAdditionObservationEvent.class, event.getGroupId(),
						event.getPersonId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on group
	 * id.
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.GROUP, GroupMembershipAdditionObservationEvent.class, groupId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} events that uses group
	 * id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroup() {
		return new SimpleEventLabeler<>(LabelerId.GROUP, GroupMembershipAdditionObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.GROUP, GroupMembershipAdditionObservationEvent.class, event.getGroupId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on person
	 * id.
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.PERSON, GroupMembershipAdditionObservationEvent.class, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} events that uses person
	 * id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForPerson() {
		return new SimpleEventLabeler<>(LabelerId.PERSON, GroupMembershipAdditionObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.PERSON, GroupMembershipAdditionObservationEvent.class, event.getPersonId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on person
	 * id and group type id.
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupTypeAndPerson(SimulationContext simulationContext, GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE_PERSON, GroupMembershipAdditionObservationEvent.class, groupTypeId, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} events that uses group
	 * type id and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroupTypeAndPerson(GroupDataManager groupDataManager) {
		return new SimpleEventLabeler<>(LabelerId.TYPE_PERSON, GroupMembershipAdditionObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
			return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE_PERSON, GroupMembershipAdditionObservationEvent.class, groupTypeId, event.getPersonId());
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on group
	 * type id.
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE, GroupMembershipAdditionObservationEvent.class, groupTypeId);
	}
	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} Matches on group type id. Automatically
	 * added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupMembershipAdditionObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
			return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE, GroupMembershipAdditionObservationEvent.class, groupTypeId);
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipAdditionObservationEvent} events. Matches on all
	 * events.
	 *
	 */
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByAll() {
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.ALL, GroupMembershipAdditionObservationEvent.class);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} all events. Automatically
	 * added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, GroupMembershipAdditionObservationEvent.class, (context, event) -> {
			return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.ALL, GroupMembershipAdditionObservationEvent.class);
		});
	}
}
