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
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import util.ContractException;

/**
 * Event to indicating that a group had a property value change
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupPropertyChangeObservationEvent implements Event {
	private final GroupId groupId;
	private final GroupPropertyId groupPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs this event from the given group id, group property id ,
	 * previous property value and current property value.
	 * 
	 */
	public GroupPropertyChangeObservationEvent(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
		super();
		this.groupId = groupId;
		this.groupPropertyId = groupPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the current property value id used to create this event
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

	/**
	 * Returns the group property id used to create this event
	 */
	public GroupPropertyId getGroupPropertyId() {
		return groupPropertyId;
	}

	/**
	 * Returns the previous property value id used to create this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	private static enum LabelerId implements EventLabelerId {
		GROUP_PROPERTY, GROUP, TYPE_PROPERTY, TYPE, ALL
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

	private static void validateGroupTypeId(Context context, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			context.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		if (!personGroupDataView.groupTypeIdExists(groupTypeId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static void validateGroupPropertyId(Context context, GroupId groupId, GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			context.throwContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		GroupTypeId groupTypeId = personGroupDataView.getGroupType(groupId);
		if (!personGroupDataView.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupTypeId + ": " + groupPropertyId);
		}
	}

	private static void validateGroupPropertyId(Context context, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			context.throwContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();

		if (!personGroupDataView.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupTypeId + ": " + groupPropertyId);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyChangeObservationEvent} events. Matches on group id
	 * and group property id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is not known</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is not known</li>
	 * 
	 */
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupAndProperty(Context context, GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupId(context, groupId);
		validateGroupPropertyId(context, groupId, groupPropertyId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.GROUP_PROPERTY, GroupPropertyChangeObservationEvent.class, groupId, groupPropertyId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group id and group property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroupAndProperty() {
		return new SimpleEventLabeler<>(LabelerId.GROUP_PROPERTY, GroupPropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class,
				LabelerId.GROUP_PROPERTY, GroupPropertyChangeObservationEvent.class, event.getGroupId(), event.getGroupPropertyId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyChangeObservationEvent} events. Matches on group id.
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
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroup(Context context, GroupId groupId) {
		validateGroupId(context, groupId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.GROUP, GroupPropertyChangeObservationEvent.class, groupId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroup() {
		return new SimpleEventLabeler<>(LabelerId.GROUP, GroupPropertyChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.GROUP, GroupPropertyChangeObservationEvent.class, event.getGroupId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyChangeObservationEvent} events. Matches on group type
	 * id and group property id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is not known</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 */
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupTypeAndProperty(Context context, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		validateGroupTypeId(context, groupTypeId);
		validateGroupPropertyId(context, groupTypeId, groupPropertyId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE_PROPERTY, GroupPropertyChangeObservationEvent.class, groupTypeId, groupPropertyId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group id and group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroupTypeAndProperty(PersonGroupDataView personGroupDataView ) {
		return new SimpleEventLabeler<>(LabelerId.TYPE_PROPERTY, GroupPropertyChangeObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(event.getGroupId());
			return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE_PROPERTY, GroupPropertyChangeObservationEvent.class, groupTypeId, event.getGroupPropertyId());
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyChangeObservationEvent} events. Matches on group type
	 * id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 * 
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 */
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupType(Context context, GroupTypeId groupTypeId) {
		validateGroupTypeId(context, groupTypeId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE, GroupPropertyChangeObservationEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroupType(PersonGroupDataView personGroupDataView) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupPropertyChangeObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(event.getGroupId());
			return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE, GroupPropertyChangeObservationEvent.class, groupTypeId);
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyChangeObservationEvent} events. Matches on all
	 * events.
	 */
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByAll() {
		return ALL_LABEL;
	}

	private final static EventLabel<GroupPropertyChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.ALL,
			GroupPropertyChangeObservationEvent.class);

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events matches all events. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, GroupPropertyChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
