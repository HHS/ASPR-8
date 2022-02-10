package plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
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

	private void validateEquality(final SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			simulationContext.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
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
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personGroupDataView == null) {
			personGroupDataView = simulationContext.getDataView(PersonGroupDataView.class).get();
		}
		final int count = personGroupDataView.getGroupTypeCountForPersonId(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupTypeCount));
	}

	private Optional<PersonId> additionRequiresRefresh(SimulationContext simulationContext, GroupMembershipAdditionObservationEvent event) {
		return Optional.of(event.getPersonId());
	}

	private Optional<PersonId> removalRequiresRefresh(SimulationContext simulationContext, GroupMembershipRemovalObservationEvent event) {
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