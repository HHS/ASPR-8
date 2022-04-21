package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * A labeler for groups. The dimension of the labeler is
 * {@linkplain GroupTypeId}, the events that stimulates a label update are
 * {@linkplain GroupMembershipAdditionEvent} and
 * {@linkplain GroupMembershipRemovalEvent} and the labeling function
 * is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class GroupLabeler implements Labeler {

	private final Function<GroupTypeCountMap, Object> groupTypeCountLabelingFunction;
	private GroupDataManager groupDataManager;

	/**
	 * Creates the Group labeler from the given labeling function
	 */
	public GroupLabeler(Function<GroupTypeCountMap, Object> groupTypeCountLabelingFunction) {
		this.groupTypeCountLabelingFunction = groupTypeCountLabelingFunction;
	}

	private Optional<PersonId> getPersonId(GroupMembershipAdditionEvent groupMembershipAdditionEvent) {
		return Optional.of(groupMembershipAdditionEvent.getPersonId());
	}

	private Optional<PersonId> getPersonId(GroupMembershipRemovalEvent groupMembershipRemovalEvent) {
		return Optional.of(groupMembershipRemovalEvent.getPersonId());
	}

	/**
	 * Returns a set of labeler sensitivitites for
	 * GroupMembershipAdditionEvent and
	 * GroupMembershipRemovalEvent. All group changes will effect the
	 * partition.
	 */
	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<GroupMembershipAdditionEvent>(GroupMembershipAdditionEvent.class, this::getPersonId));
		result.add(new LabelerSensitivity<GroupMembershipRemovalEvent>(GroupMembershipRemovalEvent.class, this::getPersonId));
		return result;
	}

	/**
	 * Returns the label for the given person id
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 */
	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}

		GroupTypeCountMap.Builder groupTypeCountMapBuilder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : groupDataManager.getGroupTypeIds()) {
			int count = groupDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			groupTypeCountMapBuilder.setCount(groupTypeId, count);
		}
		GroupTypeCountMap groupTypeCountMap = groupTypeCountMapBuilder.build();
		return groupTypeCountLabelingFunction.apply(groupTypeCountMap);
	}

	/**
	 * Returns {@link GroupTypeId} class as the dimension.
	 */
	@Override
	public Object getDimension() {
		return GroupTypeId.class;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}

		PersonId personId;
		GroupTypeId eventGroupTypeId;
		int delta;
		if (event instanceof GroupMembershipAdditionEvent) {
			GroupMembershipAdditionEvent groupMembershipAdditionEvent = (GroupMembershipAdditionEvent) event;
			personId = groupMembershipAdditionEvent.getPersonId();
			GroupId groupId = groupMembershipAdditionEvent.getGroupId();
			eventGroupTypeId = groupDataManager.getGroupType(groupId);
			delta = -1;
		} else {
			GroupMembershipRemovalEvent groupMembershipRemovalEvent = (GroupMembershipRemovalEvent) event;
			personId = groupMembershipRemovalEvent.getPersonId();
			GroupId groupId = groupMembershipRemovalEvent.getGroupId();
			eventGroupTypeId = groupDataManager.getGroupType(groupId);
			delta = +1;
		}

		GroupTypeCountMap.Builder groupTypeCountMapBuilder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : groupDataManager.getGroupTypeIds()) {
			int count = groupDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			if(groupTypeId.equals(eventGroupTypeId)) {
				count += delta;
			}
			groupTypeCountMapBuilder.setCount(groupTypeId, count);
		}
		GroupTypeCountMap groupTypeCountMap = groupTypeCountMapBuilder.build();
		return groupTypeCountLabelingFunction.apply(groupTypeCountMap);
	}

}
