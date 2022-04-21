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
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;

/**
 * Event to indicating that a group had a property value change
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class GroupPropertyUpdateEvent implements Event {
	private final GroupId groupId;
	private final GroupPropertyId groupPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs this event from the given group id, group property id ,
	 * previous property value and current property value.
	 * 
	 */
	public GroupPropertyUpdateEvent(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
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
		GROUP_PROPERTY, GROUP, TYPE_PROPERTY, TYPE
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

	private static void validateGroupPropertyId(SimulationContext simulationContext, GroupId groupId, GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
		if (!groupDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupTypeId + ": " + groupPropertyId);
		}
	}

	private static void validateGroupPropertyId(SimulationContext simulationContext, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}
		GroupDataManager groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();

		if (!groupDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID, groupTypeId + ": " + groupPropertyId);
		}
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group id and group
	 * property id.
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
	public static EventLabel<GroupPropertyUpdateEvent> getEventLabelByGroupAndProperty(SimulationContext simulationContext, GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupId(simulationContext, groupId);
		validateGroupPropertyId(simulationContext, groupId, groupPropertyId);
		return EventLabel	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PROPERTY)//
							.addKey(GroupPropertyUpdateEvent.class)//
							.addKey(groupId)//
							.addKey(groupPropertyId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyUpdateEvent} events that
	 * uses group id and group property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<GroupPropertyUpdateEvent> getEventLabelerForGroupAndProperty() {
		return EventLabeler	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.GROUP_PROPERTY)//
							.setLabelFunction((context, event) -> getEventLabelByGroupAndProperty(context, event.getGroupId(), event.getGroupPropertyId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group id.
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
	public static EventLabel<GroupPropertyUpdateEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return EventLabel	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.addKey(GroupPropertyUpdateEvent.class)//
							.addKey(groupId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyUpdateEvent} events that
	 * uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyUpdateEvent> getEventLabelerForGroup() {
		return EventLabeler	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.GROUP)//
							.setLabelFunction((context, event) -> getEventLabelByGroup(context, event.getGroupId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group type id and
	 * group property id.
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
	public static EventLabel<GroupPropertyUpdateEvent> getEventLabelByGroupTypeAndProperty(SimulationContext simulationContext, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validateGroupPropertyId(simulationContext, groupTypeId, groupPropertyId);
		return EventLabel	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.TYPE_PROPERTY)//
							.addKey(GroupPropertyUpdateEvent.class)//
							.addKey(groupTypeId)//
							.addKey(groupPropertyId)//
							.build();
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyUpdateEvent} events that
	 * uses group id and group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyUpdateEvent> getEventLabelerForGroupTypeAndProperty(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.TYPE_PROPERTY)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
								return getEventLabelByGroupTypeAndProperty(context, groupTypeId, event.getGroupPropertyId());
							})//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupPropertyUpdateEvent} events. Matches on group type id.
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
	public static EventLabel<GroupPropertyUpdateEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return EventLabel	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.addKey(GroupPropertyUpdateEvent.class)//
							.addKey(groupTypeId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyUpdateEvent} events that
	 * uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyUpdateEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return EventLabeler	.builder(GroupPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.TYPE)//
							.setLabelFunction((context, event) -> {
								GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
								return getEventLabelByGroupType(context, groupTypeId);
							})//
							.build();
	}

}
