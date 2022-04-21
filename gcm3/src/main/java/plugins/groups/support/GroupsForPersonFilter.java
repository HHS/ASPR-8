package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;

public class GroupsForPersonFilter extends Filter {

	private final Equality equality;
	private final int groupCount;
	private GroupDataManager groupDataManager;

	private void validateEquality(final SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public GroupsForPersonFilter(final Equality equality, final int groupCount) {
		this.equality = equality;
		this.groupCount = groupCount;
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
		if (groupDataManager == null) {
			groupDataManager = simulationContext.getDataManager(GroupDataManager.class);
		}
		final int count = groupDataManager.getGroupCountForPerson(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
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
