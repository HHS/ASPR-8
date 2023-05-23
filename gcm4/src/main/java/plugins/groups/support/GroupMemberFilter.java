package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.filters.Filter;
import plugins.people.support.PersonId;
import util.errors.ContractException;

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
		if (event.groupId().equals(groupId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalEvent event) {
		if (event.groupId().equals(groupId)) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GroupMemberFilter)) {
			return false;
		}
		GroupMemberFilter other = (GroupMemberFilter) obj;
		if (groupId == null) {
			if (other.groupId != null) {
				return false;
			}
		} else if (!groupId.equals(other.groupId)) {
			return false;
		}
		return true;
	}

}
