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
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class);
		if (!groupDataManager.groupExists(groupId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validateGroupTypeId(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class);
		if (!groupDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static enum LabelerId implements EventLabelerId {
		GROUP, GROUPTYPE
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
		return _getEventLabelByGroup(groupId);
	}
	
	private static EventLabel<GroupImminentRemovalEvent> _getEventLabelByGroup(GroupId groupId) {
		
		return EventLabel	.builder(GroupImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.addKey(GroupImminentRemovalEvent.class)//
							.addKey(groupId)//
							.build();
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalEvent} events
	 * that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalEvent> getEventLabelerForGroup() {
		return EventLabeler	.builder(GroupImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUP).setLabelFunction((context, event) -> _getEventLabelByGroup(event.getGroupId()))//
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
		return _getEventLabelByGroupType(groupTypeId);//
	}
	
	
	private static EventLabel<GroupImminentRemovalEvent> _getEventLabelByGroupType(GroupTypeId groupTypeId) {
		
		return EventLabel	.builder(GroupImminentRemovalEvent.class)//
							.setEventLabelerId(LabelerId.GROUPTYPE)//
							.addKey(GroupImminentRemovalEvent.class)//
							.addKey(groupTypeId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupImminentRemovalEvent.class).setEventLabelerId(LabelerId.GROUPTYPE)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
								return _getEventLabelByGroupType(groupTypeId);
							})//
							.build();
	}

	

}
