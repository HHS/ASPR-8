package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
import nucleus.NucleusError;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;
import util.ContractException;

public class GroupMemberFilter extends Filter {
	final GroupId groupId;
	private PersonGroupDataView personGroupDataView;

	private void validateGroupIdNotNull(SimulationContext simulationContext, final GroupId groupId) {
		if (groupId == null) {
			simulationContext.throwContractException(GroupError.NULL_GROUP_ID);
		}
	}

	public GroupMemberFilter(final GroupId groupId) {
		this.groupId = groupId;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		validateGroupIdNotNull(simulationContext, groupId);
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionObservationEvent event) {
		if (event.getGroupId().equals(groupId)) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalObservationEvent event) {
		if (event.getGroupId().equals(groupId)) {
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

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if(simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personGroupDataView == null) {
			personGroupDataView = simulationContext.getDataView(PersonGroupDataView.class).get();
		}
		return personGroupDataView.isGroupMember(groupId, personId);
	}

}
