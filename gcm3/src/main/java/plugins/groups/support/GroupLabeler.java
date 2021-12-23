package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Context;
import nucleus.Event;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * A labeler for groups. The dimension of the labeler is
 * {@linkplain GroupTypeId}, the events that stimulates a label update are
 * {@linkplain GroupMembershipAdditionObservationEvent} and
 * {@linkplain GroupMembershipRemovalObservationEvent} and the labeling function
 * is composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class GroupLabeler implements Labeler {

	private final Function<GroupTypeCountMap, Object> groupTypeCountLabelingFunction;
	private PersonGroupDataView personGroupDataView;

	/**
	 * Creates the Group labeler from the given labeling function
	 */
	public GroupLabeler(Function<GroupTypeCountMap, Object> groupTypeCountLabelingFunction) {
		this.groupTypeCountLabelingFunction = groupTypeCountLabelingFunction;
	}

	private Optional<PersonId> getPersonId(GroupMembershipAdditionObservationEvent groupMembershipAdditionObservationEvent) {
		return Optional.of(groupMembershipAdditionObservationEvent.getPersonId());
	}

	private Optional<PersonId> getPersonId(GroupMembershipRemovalObservationEvent groupMembershipRemovalObservationEvent) {
		return Optional.of(groupMembershipRemovalObservationEvent.getPersonId());
	}

	/**
	 * Returns a set of labeler sensitivitites for
	 * GroupMembershipAdditionObservationEvent and
	 * GroupMembershipRemovalObservationEvent. All group changes will effect the
	 * partition.
	 */
	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<GroupMembershipAdditionObservationEvent>(GroupMembershipAdditionObservationEvent.class, this::getPersonId));
		result.add(new LabelerSensitivity<GroupMembershipRemovalObservationEvent>(GroupMembershipRemovalObservationEvent.class, this::getPersonId));
		return result;
	}

	/**
	 * Returns the label for the given person id
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
	 */
	@Override
	public Object getLabel(Context context, PersonId personId) {
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}

		GroupTypeCountMap.Builder groupTypeCountMapBuilder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : personGroupDataView.getGroupTypeIds()) {
			int count = personGroupDataView.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
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
	public Object getPastLabel(Context context, Event event) {
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}

		PersonId personId;
		GroupTypeId eventGroupTypeId;
		int delta;
		if (event instanceof GroupMembershipAdditionObservationEvent) {
			GroupMembershipAdditionObservationEvent groupMembershipAdditionObservationEvent = (GroupMembershipAdditionObservationEvent) event;
			personId = groupMembershipAdditionObservationEvent.getPersonId();
			GroupId groupId = groupMembershipAdditionObservationEvent.getGroupId();
			eventGroupTypeId = personGroupDataView.getGroupType(groupId);
			delta = -1;
		} else {
			GroupMembershipRemovalObservationEvent groupMembershipRemovalObservationEvent = (GroupMembershipRemovalObservationEvent) event;
			personId = groupMembershipRemovalObservationEvent.getPersonId();
			GroupId groupId = groupMembershipRemovalObservationEvent.getGroupId();
			eventGroupTypeId = personGroupDataView.getGroupType(groupId);
			delta = +1;
		}

		GroupTypeCountMap.Builder groupTypeCountMapBuilder = GroupTypeCountMap.builder();
		for (GroupTypeId groupTypeId : personGroupDataView.getGroupTypeIds()) {
			int count = personGroupDataView.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
			if(groupTypeId.equals(eventGroupTypeId)) {
				count += delta;
			}
			groupTypeCountMapBuilder.setCount(groupTypeId, count);
		}
		GroupTypeCountMap groupTypeCountMap = groupTypeCountMapBuilder.build();
		return groupTypeCountLabelingFunction.apply(groupTypeCountMap);
	}

}
