package plugins.regions.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
import nucleus.NucleusError;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import util.ContractException;

public final class RegionFilter extends Filter {

	private final Set<RegionId> regionIds = new LinkedHashSet<>();

	private RegionLocationDataView regionLocationDataView;

	private void validateRegionId(SimulationContext simulationContext, RegionDataView regionDataView, final RegionId regionId) {

		if (regionId == null) {
			simulationContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataView.regionIdExists(regionId)) {
			simulationContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	public RegionFilter(final RegionId... regionIds) {
		for (RegionId regionId : regionIds) {
			this.regionIds.add(regionId);
		}
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		RegionDataView regionDataView = simulationContext.getDataView(RegionDataView.class).get();

		for (RegionId regionId : regionIds) {
			validateRegionId(simulationContext, regionDataView, regionId);
		}

	}

	public RegionFilter(final Set<RegionId> regionIds) {
		this.regionIds.addAll(regionIds);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (regionLocationDataView == null) {
			regionLocationDataView = simulationContext.getDataView(RegionLocationDataView.class).get();
		}
		return regionIds.contains(regionLocationDataView.getPersonRegion(personId));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegionFilter [regionIds=");
		builder.append(regionIds);
		builder.append("]");
		return builder.toString();
	}

	private Optional<PersonId> requiresRefresh(SimulationContext simulationContext, PersonRegionChangeObservationEvent event) {
		boolean previousRegionIdContained = regionIds.contains(event.getPreviousRegionId());
		boolean currentRegionIdContained = regionIds.contains(event.getCurrentRegionId());
		if (previousRegionIdContained != currentRegionIdContained) {
			return Optional.of(event.getPersonId());
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonRegionChangeObservationEvent>(PersonRegionChangeObservationEvent.class, this::requiresRefresh));
		return result;
	}

}
