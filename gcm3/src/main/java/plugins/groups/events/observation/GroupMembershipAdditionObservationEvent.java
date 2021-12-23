package plugins.groups.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

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

	private static void validateGroupId(Context context, GroupId groupId) {
		if (groupId == null) {
			context.throwContractException(GroupError.NULL_GROUP_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();

		if (!personGroupDataView.groupExists(groupId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validatePersonId(Context context, PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
		PersonDataView personDataView = context.getDataView(PersonDataView.class).get();
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validateGroupTypeId(Context context, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			context.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		if (!personGroupDataView.groupTypeIdExists(groupTypeId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupAndPerson(Context context, GroupId groupId, PersonId personId) {
		validateGroupId(context, groupId);
		validatePersonId(context, personId);
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroup(Context context, GroupId groupId) {
		validateGroupId(context, groupId);
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByPerson(Context context, PersonId personId) {
		validatePersonId(context, personId);
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupTypeAndPerson(Context context, GroupTypeId groupTypeId, PersonId personId) {
		validateGroupTypeId(context, groupTypeId);
		validatePersonId(context, personId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE_PERSON, GroupMembershipAdditionObservationEvent.class, groupTypeId, personId);
	}

	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} events that uses group
	 * type id and person id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroupTypeAndPerson(PersonGroupDataView personGroupDataView) {
		return new SimpleEventLabeler<>(LabelerId.TYPE_PERSON, GroupMembershipAdditionObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
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
	public static EventLabel<GroupMembershipAdditionObservationEvent> getEventLabelByGroupType(Context context, GroupTypeId groupTypeId) {
		validateGroupTypeId(context, groupTypeId);
		return new MultiKeyEventLabel<>(GroupMembershipAdditionObservationEvent.class, LabelerId.TYPE, GroupMembershipAdditionObservationEvent.class, groupTypeId);
	}
	/**
	 * Returns an event labeler for
	 * {@link GroupMembershipAdditionObservationEvent} Matches on group type id. Automatically
	 * added at initialization.
	 */
	public static EventLabeler<GroupMembershipAdditionObservationEvent> getEventLabelerForGroupType(PersonGroupDataView personGroupDataView) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupMembershipAdditionObservationEvent.class, (context, event) -> {
			GroupId groupId = event.getGroupId();
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
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
