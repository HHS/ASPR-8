package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;

public final class GroupTypesForPersonFilter extends Filter {

	private final Equality equality;
	private final int groupTypeCount;
	private GroupsDataManager groupsDataManager;

	private void validateEquality(final SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public GroupTypesForPersonFilter(final Equality equality, final int groupTypeCount) {
		this.equality = equality;
		this.groupTypeCount = groupTypeCount;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		validateEquality(simulationContext, equality);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}
		final int count = groupsDataManager.getGroupTypeCountForPersonId(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupTypeCount));
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionEvent event) {
		return Optional.of(event.getPersonId());
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalEvent event) {
		return Optional.of(event.getPersonId());
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionEvent>(GroupMembershipAdditionEvent.class, this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalEvent>(GroupMembershipRemovalEvent.class, this::removalRequiresRefresh));

		return result;
	}

}