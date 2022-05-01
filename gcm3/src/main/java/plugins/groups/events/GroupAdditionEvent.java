package plugins.groups.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import util.errors.ContractException;

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
		TYPE
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
		GroupsDataManager groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		if (!groupsDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID);
		}
		return _getEventLabelByGroupType(groupTypeId);
	}
	
	private static EventLabel<GroupAdditionEvent> _getEventLabelByGroupType(GroupTypeId groupTypeId) {
		
		return EventLabel	.builder(GroupAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.addKey(GroupAdditionEvent.class)//
							.addKey(groupTypeId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupAdditionEvent} events that uses
	 * group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupAdditionEvent> getEventLabelerForGroupType(GroupsDataManager groupsDataManager) {
		return EventLabeler	.builder(GroupAdditionEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupsDataManager.getGroupType(event.getGroupId());
								return _getEventLabelByGroupType(groupTypeId);
							})//
							.build();
	}

}
