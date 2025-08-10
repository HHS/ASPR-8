package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events.PersonRegionUpdateEvent;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class RegionFilter extends Filter {

	private final Set<RegionId> regionIds = new LinkedHashSet<>();

	private RegionsDataManager regionsDataManager;

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionsDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	public RegionFilter(final RegionId... regionIds) {
		for (RegionId regionId : regionIds) {
			this.regionIds.add(regionId);
		}
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (regionsDataManager == null) {
			regionsDataManager = partitionsContext.getDataManager(RegionsDataManager.class);
		}

		for (RegionId regionId : regionIds) {
			validateRegionId(regionId);
		}

	}

	public Set<RegionId> getRegionIds() {
		return new LinkedHashSet<>(regionIds);
	}

	public RegionFilter(final Set<RegionId> regionIds) {
		this.regionIds.addAll(regionIds);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (regionsDataManager == null) {
			regionsDataManager = partitionsContext.getDataManager(RegionsDataManager.class);
		}
		return regionIds.contains(regionsDataManager.getPersonRegion(personId));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegionFilter [regionIds=");
		builder.append(regionIds);
		builder.append("]");
		return builder.toString();
	}

	private Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, PersonRegionUpdateEvent event) {
		boolean previousRegionIdContained = regionIds.contains(event.previousRegionId());
		boolean currentRegionIdContained = regionIds.contains(event.currentRegionId());
		if (previousRegionIdContained != currentRegionIdContained) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(
				new FilterSensitivity<PersonRegionUpdateEvent>(PersonRegionUpdateEvent.class, this::requiresRefresh));
		return result;
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(regionIds);
	}

	/**
     * Two {@link RegionFilter} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegionFilter other = (RegionFilter) obj;
		return Objects.equals(regionIds, other.regionIds);
	}

}
