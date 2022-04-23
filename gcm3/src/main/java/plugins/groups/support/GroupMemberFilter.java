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
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;

public class GroupMemberFilter extends Filter {
	final GroupId groupId;
	private GroupsDataManager groupsDataManager;

	private void validateGroupIdNotNull(SimulationContext simulationContext, final GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
	}

	public GroupMemberFilter(final GroupId groupId) {
		this.groupId = groupId;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		validateGroupIdNotNull(simulationContext, groupId);
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionEvent event) {
		if (event.getGroupId().equals(groupId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalEvent event) {
		if (event.getGroupId().equals(groupId)) {
			return Optional.of(event.getPersonId());
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

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (groupsDataManager == null) {
			groupsDataManager = simulationContext.getDataManager(GroupsDataManager.class);
		}
		return groupsDataManager.isPersonInGroup(personId,groupId);
	}

}
