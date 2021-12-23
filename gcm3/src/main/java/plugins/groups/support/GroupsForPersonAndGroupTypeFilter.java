package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;

public final class GroupsForPersonAndGroupTypeFilter extends Filter {
	private final GroupTypeId groupTypeId;
	private final Equality equality;
	private final int groupCount;
	private PersonGroupDataView personGroupDataView;

	private void validateEquality(final Context context, final Equality equality) {
		if (equality == null) {
			context.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateGroupTypeId(final Context context, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			context.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}

		if (!personGroupDataView.groupTypeIdExists(groupTypeId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	public GroupsForPersonAndGroupTypeFilter(final GroupTypeId groupTypeId, final Equality equality, final int groupCount) {
		this.equality = equality;
		this.groupCount = groupCount;
		this.groupTypeId = groupTypeId;
	}

	@Override
	public void validate(Context context) {
		validateEquality(context, equality);
		validateGroupTypeId(context, groupTypeId);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}
		final int count = personGroupDataView.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
		return evaluate(count);
	}

	private boolean evaluate(int count) {
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
	}

	private Optional<PersonId> additionRequiresRefresh(Context context, GroupMembershipAdditionObservationEvent event) {
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}
		if (personGroupDataView.getGroupType(event.getGroupId()).equals(groupTypeId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(Context context, GroupMembershipRemovalObservationEvent event) {
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}
		if (personGroupDataView.getGroupType(event.getGroupId()).equals(groupTypeId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionObservationEvent>(GroupMembershipAdditionObservationEvent.class, this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalObservationEvent>(GroupMembershipRemovalObservationEvent.class, this::removalRequiresRefresh));

		return result;
	}

}