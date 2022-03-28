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
 * Event to indicating that person was removed from a group
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupMembershipRemovalObservationEvent implements Event {
	private final PersonId personId;
	private final GroupId groupId;

	/**
	 * Constructs this event from the given person id and group id
	 * 
	 */
	public GroupMembershipRemovalObservationEvent(final PersonId personId, final GroupId groupId) {
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
		GroupDataManager personGroupDataView = simulationContext.getDataManager(GroupDataManager.class).get();

		if (!personGroupDataView.groupExists(groupId)) {
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
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on group
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
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByGroupAndPerson(SimulationContext simulationContext, GroupId groupId, PersonId personId) {
		validateGroupId(simulationContext, groupId);
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.GROUP_PERSON, GroupMembershipRemovalObservationEvent.class, groupId, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} events that uses group id
	 * and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForGroupAndPerson() {
		return new SimpleEventLabeler<>(LabelerId.GROUP_PERSON, GroupMembershipRemovalObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class,
				LabelerId.GROUP_PERSON, GroupMembershipRemovalObservationEvent.class, event.getGroupId(), event.getPersonId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on group
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
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.GROUP, GroupMembershipRemovalObservationEvent.class, groupId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} events that uses group id.
	 * Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForGroup() {
		return new SimpleEventLabeler<>(LabelerId.GROUP, GroupMembershipRemovalObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.GROUP, GroupMembershipRemovalObservationEvent.class, event.getGroupId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on person
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
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByPerson(SimulationContext simulationContext, PersonId personId) {
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.PERSON, GroupMembershipRemovalObservationEvent.class, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} events that uses person
	 * id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForPerson() {
		return new SimpleEventLabeler<>(LabelerId.PERSON, GroupMembershipRemovalObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.PERSON, GroupMembershipRemovalObservationEvent.class, event.getPersonId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on person
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
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByGroupTypeAndPerson(SimulationContext simulationContext, GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validatePersonId(simulationContext, personId);
		return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.TYPE_PERSON, GroupMembershipRemovalObservationEvent.class, groupTypeId, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} events that uses group
	 * type id and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForGroupTypeAndPerson(GroupDataManager groupDataManager) {
		return new SimpleEventLabeler<>(LabelerId.TYPE_PERSON, GroupMembershipRemovalObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
			return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.TYPE_PERSON, GroupMembershipRemovalObservationEvent.class, groupTypeId, event.getPersonId());
		});

	}

	private static enum LabelerId implements EventLabelerId {
		GROUP_PERSON, GROUP, PERSON, TYPE_PERSON, TYPE, ALL
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on group
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
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.TYPE, GroupMembershipRemovalObservationEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} Matches on group type id.
	 * Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupMembershipRemovalObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
			return new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.TYPE, GroupMembershipRemovalObservationEvent.class, groupTypeId);
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupMembershipRemovalObservationEvent} events. Matches on all
	 * events.
	 *
	 */
	public static EventLabel<GroupMembershipRemovalObservationEvent> getEventLabelByAll() {
		return ALL_EVENT_LABEL_INSTANCE;
	}

	private static EventLabel<GroupMembershipRemovalObservationEvent> ALL_EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(GroupMembershipRemovalObservationEvent.class, LabelerId.ALL,
			GroupMembershipRemovalObservationEvent.class);

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipRemovalObservationEvent} all events. Automatically
	 * added at initialization.
	 */
	public static EventLabeler<GroupMembershipRemovalObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, GroupMembershipRemovalObservationEvent.class, (context, event) -> ALL_EVENT_LABEL_INSTANCE);
	}

}
