package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.GroupMembershipRemovalObservationEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;

public final class GroupsForPersonAndGroupTypeFilter extends Filter {
	private final GroupTypeId groupTypeId;
	private final Equality equality;
	private final int groupCount;
	private GroupDataManager groupDataManager;

	private void validateEquality(final SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateGroupTypeId(final SimulationContext simulationContext, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}

		if (!groupDataManager.groupTypeIdExists(groupTypeId)) {
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
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}
		final int count = groupDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
		return evaluate(count);
	}

	private boolean evaluate(int count) {
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionObservationEvent event) {
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}
		if (groupDataManager.getGroupType(event.getGroupId()).equals(groupTypeId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalObservationEvent event) {
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class).get();
		}
		if (groupDataManager.getGroupType(event.getGroupId()).equals(groupTypeId)) {
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