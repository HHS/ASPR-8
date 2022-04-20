package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;

/**
 * An event indicating that a group has been created
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupAdditionEvent implements Event {

	private final GroupId groupId;

	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupAdditionEvent(final GroupId groupId) {
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
	 * Returns an event label used to subscribe to {@link GroupAdditionEvent}
	 * events. Matches on group type id.
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
	public static EventLabel<GroupAdditionEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		if (!groupDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}
		return new MultiKeyEventLabel<>(GroupAdditionEvent.class, LabelerId.TYPE, GroupAdditionEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupAdditionEvent} events that uses
	 * group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupAdditionEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
								return getEventLabelByGroupType(context, groupTypeId);
							})//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to {@link GroupAdditionEvent}
	 * events. Matches on all events.
	 *
	 *
	 */
	public static EventLabel<GroupAdditionEvent> getEventLabelByAll() {
		return ALL_EVENT_LABEL_INSTANCE;
	}

	private final static EventLabel<GroupAdditionEvent> ALL_EVENT_LABEL_INSTANCE = new MultiKeyEventLabel<>(GroupAdditionEvent.class, LabelerId.ALL, GroupAdditionEvent.class);

	/**
	 * Returns an event labeler for {@link GroupAdditionEvent} events that
	 * matches all events. Automatically added at initialization.
	 */
	public static EventLabeler<GroupAdditionEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(GroupAdditionEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_EVENT_LABEL_INSTANCE)//
							.build();
	}
}
