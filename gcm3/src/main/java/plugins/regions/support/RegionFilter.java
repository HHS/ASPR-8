package plugins.regions.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.PersonRegionChangeObservationEvent;
import plugins.regions.datamanagers.RegionDataManager;

public final class RegionFilter extends Filter {

	private final Set<RegionId> regionIds = new LinkedHashSet<>();

	private RegionDataManager regionDataManager;

	private void validateRegionId( final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	public RegionFilter(final RegionId... regionIds) {
		for (RegionId regionId : regionIds) {
			this.regionIds.add(regionId);
		}
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (regionDataManager == null) {
			regionDataManager = simulationContext.getDataManager(RegionDataManager.class).get();
		}
		
		for (RegionId regionId : regionIds) {
			validateRegionId(regionId);
		}

	}

	public RegionFilter(final Set<RegionId> regionIds) {
		this.regionIds.addAll(regionIds);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (regionDataManager == null) {
			regionDataManager = simulationContext.getDataManager(RegionDataManager.class).get();
		}
		return regionIds.contains(regionDataManager.getPersonRegion(personId));
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
