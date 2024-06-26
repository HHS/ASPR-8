package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.events.PersonResourceUpdateEvent;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class ResourceFilter extends Filter {
	private final ResourceId resourceId;
	private final long resourceValue;
	private final Equality equality;
	private ResourcesDataManager resourcesDataManager;

	private void validateResourceId(PartitionsContext partitionsContext, final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}

		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourcesDataManager == null) {
			resourcesDataManager = partitionsContext.getDataManager(ResourcesDataManager.class);
		}

		if (!resourcesDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateEquality(PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public Equality getEquality() {
		return equality;
	}

	public long getResourceValue() {
		return resourceValue;
	}

	public ResourceFilter(final ResourceId resourceId, final Equality equality, final long resourceValue) {
		this.resourceId = resourceId;
		this.resourceValue = resourceValue;
		this.equality = equality;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateEquality(partitionsContext, equality);
		validateResourceId(partitionsContext, resourceId);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}

		if (resourcesDataManager == null) {
			resourcesDataManager = partitionsContext.getDataManager(ResourcesDataManager.class);
		}

		final long level = resourcesDataManager.getPersonResourceLevel(resourceId, personId);
		return equality.isCompatibleComparisonValue(Long.compare(level, resourceValue));
	}

	private Optional<PersonId> requiresRefresh(PartitionsContext partitionsContext, PersonResourceUpdateEvent event) {
		if (event.resourceId().equals(resourceId)) {
			long previousResourceLevel = event.previousResourceLevel();
			long currentResourceLevel = event.currentResourceLevel();
			if (equality.isCompatibleComparisonValue(Long.compare(previousResourceLevel, resourceValue)) != equality
					.isCompatibleComparisonValue(Long.compare(currentResourceLevel, resourceValue))) {
				return Optional.of(event.personId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<PersonResourceUpdateEvent>(PersonResourceUpdateEvent.class,
				this::requiresRefresh));
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equality == null) ? 0 : equality.hashCode());
		result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
		result = prime * result + (int) (resourceValue ^ (resourceValue >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ResourceFilter)) {
			return false;
		}
		ResourceFilter other = (ResourceFilter) obj;
		if (equality != other.equality) {
			return false;
		}
		if (resourceId == null) {
			if (other.resourceId != null) {
				return false;
			}
		} else if (!resourceId.equals(other.resourceId)) {
			return false;
		}
		if (resourceValue != other.resourceValue) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceFilter [resourceId=");
		builder.append(resourceId);
		builder.append(", resourceValue=");
		builder.append(resourceValue);
		builder.append(", equality=");
		builder.append(equality);
		builder.append("]");
		return builder.toString();
	}

}
