package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;
import util.ContractException;

public final class GroupTypesForPersonFilter extends Filter {

	private final Equality equality;
	private final int groupTypeCount;
	private PersonGroupDataView personGroupDataView;

	private void validateEquality(final Context context, final Equality equality) {
		if (equality == null) {
			context.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public GroupTypesForPersonFilter(final Equality equality, final int groupTypeCount) {
		this.equality = equality;
		this.groupTypeCount = groupTypeCount;
	}

	@Override
	public void validate(Context context) {
		validateEquality(context, equality);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		if(context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personGroupDataView == null) {
			personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		}
		final int count = personGroupDataView.getGroupTypeCountForPersonId(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupTypeCount));
	}

	private Optional<PersonId> additionRequiresRefresh(Context context, GroupMembershipAdditionObservationEvent event) {
		return Optional.of(event.getPersonId());
	}

	private Optional<PersonId> removalRequiresRefresh(Context context, GroupMembershipRemovalObservationEvent event) {
		return Optional.of(event.getPersonId());
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionObservationEvent>(GroupMembershipAdditionObservationEvent.class, this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalObservationEvent>(GroupMembershipRemovalObservationEvent.class, this::removalRequiresRefresh));

		return result;
	}

}