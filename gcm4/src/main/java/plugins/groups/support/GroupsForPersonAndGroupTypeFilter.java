package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.filters.Filter;
import plugins.people.support.PersonId;
import util.errors.ContractException;

public final class GroupsForPersonAndGroupTypeFilter extends Filter {
	private final GroupTypeId groupTypeId;
	private final Equality equality;
	private final int groupCount;
	private GroupsDataManager groupsDataManager;

	private void validateEquality(final SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateGroupTypeId(final SimulationContext simulationContext, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}

		if (!groupsDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	public GroupsForPersonAndGroupTypeFilter(final GroupTypeId groupTypeId, final Equality equality, final int groupCount) {
		this.equality = equality;
		this.groupCount = groupCount;
		this.groupTypeId = groupTypeId;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		validateEquality(simulationContext, equality);
		validateGroupTypeId(simulationContext, groupTypeId);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}
		final int count = groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
		return evaluate(count);
	}

	private boolean evaluate(int count) {
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionEvent event) {
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}
		if (groupsDataManager.getGroupType(event.groupId()).equals(groupTypeId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalEvent event) {
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}
		if (groupsDataManager.getGroupType(event.groupId()).equals(groupTypeId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionEvent>(GroupMembershipAdditionEvent.class, this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalEvent>(GroupMembershipRemovalEvent.class, this::removalRequiresRefresh));

		return result;
	}

}