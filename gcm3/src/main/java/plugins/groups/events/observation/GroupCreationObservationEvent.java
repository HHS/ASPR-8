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
import util.ContractException;

/**
 * An event indicating that a group has been created
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupCreationObservationEvent implements Event {

	private final GroupId groupId;

	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupCreationObservationEvent(final GroupId groupId) {
		this.groupId = groupId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
	}

	private static enum LabelerId implements EventLabelerId {
		TYPE, ALL
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupCreationObservationEvent} events. Matches on group type id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is not known</li>
	 * 
	 */
	public static EventLabel<GroupCreationObservationEvent> getEventLabelByGroupType(Context context, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			context.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		PersonGroupDataView personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		if (!personGroupDataView.groupTypeIdExists(groupTypeId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}
		return new MultiKeyEventLabel<>(GroupCreationObservationEvent.class, LabelerId.TYPE, GroupCreationObservationEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupCreationObservationEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupCreationObservationEvent> getEventLabelerForGroupType(PersonGroupDataView personGroupDataView) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupCreationObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(event.getGroupId());
			return new MultiKeyEventLabel<>(GroupCreationObservationEvent.class, LabelerId.TYPE, GroupCreationObservationEvent.class, groupTypeId);
		});
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupCreationObservationEvent} events. Matches on all events.
	 *
	 *
	 */
	public static EventLabel<GroupCreationObservationEvent> getEventLabelByAll() {
		return ALL_EVENT_LABEL_INSTANCE;
	}

	private final static EventLabel<GroupCreationObservationEvent> ALL_EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(GroupCreationObservationEvent.class, LabelerId.ALL,
			GroupCreationObservationEvent.class);

	/**
	 * Returns an event labeler for {@link GroupCreationObservationEvent} events
	 * that matches all events. Automatically added at initialization.
	 */
	public static EventLabeler<GroupCreationObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, GroupCreationObservationEvent.class, (context, event) -> ALL_EVENT_LABEL_INSTANCE);
	}
}
