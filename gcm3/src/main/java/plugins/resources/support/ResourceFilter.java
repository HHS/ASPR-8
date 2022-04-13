package plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.events.PersonResourceUpdateEvent;

public final class ResourceFilter extends Filter {
	private final ResourceId resourceId;
	private final long resourceValue;
	private final Equality equality;
	private ResourceDataManager resourceDataManager;

	private void validateResourceId(SimulationContext simulationContext, final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourceDataManager == null) {
			resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class).get();
		}

		if (!resourceDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateEquality(SimulationContext simulationContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public ResourceFilter(final ResourceId resourceId, final Equality equality, final long resourceValue) {
		this.resourceId = resourceId;
		this.resourceValue = resourceValue;
		this.equality = equality;
	}

	@Override
	public void validate(SimulationContext simulationContext) {
		validateEquality(simulationContext, equality);
		validateResourceId(simulationContext, resourceId);
	}

	@Override
	public boolean evaluate(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourceDataManager == null) {
			resourceDataManager = simulationContext.getDataManager(ResourceDataManager.class).get();
		}
		
		final long level = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		return equality.isCompatibleComparisonValue(Long.compare(level, resourceValue));
	}

	private Optional<PersonId> requiresRefresh(SimulationContext simulationContext, PersonResourceUpdateEvent event) {
		if (event.getResourceId().equals(resourceId)) {
			long previousResourceLevel = event.getPreviousResourceLevel();
			long currentResourceLevel = event.getCurrentResourceLevel();
			if (equality.isCompatibleComparisonValue(Long.compare(previousResourceLevel, resourceValue)) != equality.isCompatibleComparisonValue(Long.compare(currentResourceLevel, resourceValue))) {
				return Optional.of(event.getPersonId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonResourceUpdateEvent>(PersonResourceUpdateEvent.class, this::requiresRefresh));
		return result;
	}

}
