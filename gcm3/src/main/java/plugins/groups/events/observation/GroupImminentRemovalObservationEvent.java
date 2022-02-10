package plugins.groups.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
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
 * Event to signal the imminent removal of a group from the simulation
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GroupImminentRemovalObservationEvent implements Event {
	private final GroupId groupId;
	/**
	 * Constructs this event from the group id
	 * 
	 */
	public GroupImminentRemovalObservationEvent(final GroupId groupId) {
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
			simulationContext.throwContractException(GroupError.NULL_GROUP_ID);
		}
		PersonGroupDataView personGroupDataView = simulationContext.getDataView(PersonGroupDataView.class).get();
		if (!personGroupDataView.groupExists(groupId)) {
			simulationContext.throwContractException(GroupError.UNKNOWN_GROUP_ID, groupId);
		}
	}

	private static void validateGroupTypeId(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			simulationContext.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		PersonGroupDataView personGroupDataView = simulationContext.getDataView(PersonGroupDataView.class).get();
		if (!personGroupDataView.groupTypeIdExists(groupTypeId)) {
			simulationContext.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	private static enum LabelerId implements EventLabelerId {
		GROUP, GROUPTYPE, ALL
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalObservationEvent} events. Matches on group id.
	 *
	 * Preconditions : The context cannot be null
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group
	 *             id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the
	 *             group id is not known</li>
	 * 
	 */
	public static EventLabel<GroupImminentRemovalObservationEvent> getEventLabelByGroup(SimulationContext simulationContext, GroupId groupId) {
		validateGroupId(simulationContext, groupId);
		return new MultiKeyEventLabel<>(GroupImminentRemovalObservationEvent.class, LabelerId.GROUP, GroupImminentRemovalObservationEvent.class, groupId);
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalObservationEvent} events
	 * that uses group id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalObservationEvent> getEventLabelerForGroup() {
		return new SimpleEventLabeler<>(LabelerId.GROUP, GroupImminentRemovalObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(GroupImminentRemovalObservationEvent.class, LabelerId.GROUP, GroupImminentRemovalObservationEvent.class, event.getGroupId()));
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalObservationEvent} events. Matches on group type id.
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
	public static EventLabel<GroupImminentRemovalObservationEvent> getEventLabelByGroupType(SimulationContext simulationContext, GroupTypeId groupTypeId) {
		validateGroupTypeId(simulationContext, groupTypeId);
		return new MultiKeyEventLabel<>(GroupImminentRemovalObservationEvent.class, LabelerId.GROUPTYPE, GroupImminentRemovalObservationEvent.class, groupTypeId);
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalObservationEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalObservationEvent> getEventLabelerForGroupType(PersonGroupDataView personGroupDataView) {
		return new SimpleEventLabeler<>(LabelerId.GROUPTYPE, GroupImminentRemovalObservationEvent.class, (context, event) -> {
			GroupTypeId groupTypeId = personGroupDataView.getGroupType(event.getGroupId());
			return new MultiKeyEventLabel<>(GroupImminentRemovalObservationEvent.class, LabelerId.GROUPTYPE, GroupImminentRemovalObservationEvent.class, groupTypeId);
		});
	}

	private static EventLabel<GroupImminentRemovalObservationEvent> ALL_EVENTS_LABEL = new MultiKeyEventLabel<>(GroupImminentRemovalObservationEvent.class, LabelerId.ALL, GroupImminentRemovalObservationEvent.class);

	/**
	 * Returns an event label used to subscribe to
	 * {@link GroupImminentRemovalObservationEvent} events. Matches on all events.
	 *
	 * 
	 */
	public static EventLabel<GroupImminentRemovalObservationEvent> getEventLabelByAll() {
		return ALL_EVENTS_LABEL;
	}

	/**
	 * Returns an event labeler for {@link GroupImminentRemovalObservationEvent} events
	 * that uses group type id. Automatically added at initialization.
	 */
	public static EventLabeler<GroupImminentRemovalObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, GroupImminentRemovalObservationEvent.class, (context, event) -> ALL_EVENTS_LABEL);
	}

}
