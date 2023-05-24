package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.support.filters.Filter;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class GroupTypesForPersonFilter extends Filter {

	private final Equality equality;
	private final int groupTypeCount;
	private GroupsDataManager groupsDataManager;

	private void validateEquality(final PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public GroupTypesForPersonFilter(final Equality equality, final int groupTypeCount) {
		this.equality = equality;
		this.groupTypeCount = groupTypeCount;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateEquality(partitionsContext, equality);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		final int count = groupsDataManager.getGroupTypeCountForPersonId(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupTypeCount));
	}

	private Optional<PersonId> additionRequiresRefresh(PartitionsContext partitionsContext, GroupMembershipAdditionEvent event) {
		return Optional.of(event.personId());
	}

	private Optional<PersonId> removalRequiresRefresh(PartitionsContext partitionsContext, GroupMembershipRemovalEvent event) {
		return Optional.of(event.personId());
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionEvent>(GroupMembershipAdditionEvent.class, this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalEvent>(GroupMembershipRemovalEvent.class, this::removalRequiresRefresh));

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equality == null) ? 0 : equality.hashCode());
		result = prime * result + groupTypeCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GroupTypesForPersonFilter)) {
			return false;
		}
		GroupTypesForPersonFilter other = (GroupTypesForPersonFilter) obj;
		if (equality != other.equality) {
			return false;
		}
		if (groupTypeCount != other.groupTypeCount) {
			return false;
		}
		return true;
	}

}