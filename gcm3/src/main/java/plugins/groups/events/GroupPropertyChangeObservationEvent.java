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
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;

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
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupAndProperty(SimulationContext simulationContext, GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupId(simulationContext, groupId);
		validateGroupPropertyId(simulationContext, groupId, groupPropertyId);
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
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
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
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupTypeAndProperty(SimulationContext simulationContext, GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		validateGroupPropertyId(simulationContext, groupTypeId, groupPropertyId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE_PROPERTY, GroupPropertyChangeObservationEvent.class, groupTypeId, groupPropertyId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group id and group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroupTypeAndProperty(GroupDataManager groupDataManager ) {
		return new SimpleEventLabeler<>(LabelerId.TYPE_PROPERTY, GroupPropertyChangeObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
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
	public static EventLabel<GroupPropertyChangeObservationEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return new MultiKeyEventLabel<>(GroupPropertyChangeObservationEvent.class, LabelerId.TYPE, GroupPropertyChangeObservationEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupPropertyChangeObservationEvent}
	 * events that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupPropertyChangeObservationEvent> getEventLabelerForGroupType(GroupDataManager groupDataManager) {
		return new SimpleEventLabeler<>(LabelerId.TYPE, GroupPropertyChangeObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = groupDataManager.getGroupType(event.getGroupId());
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
