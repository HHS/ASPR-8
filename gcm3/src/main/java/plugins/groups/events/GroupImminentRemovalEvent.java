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
 * Event to signal the imminent removal of a group from the simulation
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GroupImminentRemovalEvent implements Event {
	private final GroupId groupId;

	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupImminentRemovalEvent(final GroupId groupId) {
		super();
		this.groupId = groupId;
	}

	/**
	 * Returns the group id used to create this event
	 */
	public GroupId getGroupId() {
		return groupId;
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

	private static void validateGroupTypeId(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		if (!groupDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static enum LabelerId implements EventLabelerId {
		GROUP, GROUPTYPE, ALL
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches on group id.
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
	public static EventLabel<GroupImminentRemovalEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return new MultiKeyEventLabel<>(GroupImminentRemovalEvent.class, LabelerId.GROUP, GroupImminentRemovalEvent.class, groupId);
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalEvent} events
	 * that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalEvent> getEventLabelerForGroup() {
		return EventLabeler	.builder(GroupImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)
							.setLabelFunction((context, event) -> getEventLabelByGroup(context, event.getGroupId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches on group type id.
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
	public static EventLabel<GroupImminentRemovalEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return new MultiKeyEventLabel<>(GroupImminentRemovalEvent.class, LabelerId.GROUPTYPE, GroupImminentRemovalEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupImminentRemovalEvent.class).setEventLabelerId(LabelerId.GROUPTYPE)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
								return getEventLabelByGroupType(context, groupTypeId);
							})//
							.build();
	}

	private static EventLabel<GroupImminentRemovalEvent> ALL_EVENTS_LABEL = new MultiKeyEventLabel<>(GroupImminentRemovalEvent.class, LabelerId.ALL, GroupImminentRemovalEvent.class);

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalEvent} events. Matches on all events.
	 *
	 * 
	 */
	public static EventLabel<GroupImminentRemovalEvent> getEventLabelByAll() {
		return ALL_EVENTS_LABEL;
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalEvent> getEventLabelerForAll() {
		return EventLabeler	.builder(GroupImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.ALL)//
							.setLabelFunction((context, event) -> ALL_EVENTS_LABEL)//
							.build();
	}

}
