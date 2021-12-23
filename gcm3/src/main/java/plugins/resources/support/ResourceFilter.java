package plugins.resources.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.support.PersonId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import util.ContractException;

public final class ResourceFilter extends Filter {
	private final ResourceId resourceId;
	private final long resourceValue;
	private final Equality equality;
	private ResourceDataView resourceDataView;

	private void validateResourceId(Context context, final ResourceId resourceId) {
		if (resourceId == null) {
			context.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (resourceDataView == null) {
			resourceDataView = context.getDataView(ResourceDataView.class).get();
		}

		if (!resourceDataView.resourceIdExists(resourceId)) {
			context.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateEquality(Context context, final Equality equality) {
		if (equality == null) {
			context.throwContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public ResourceFilter(final ResourceId resourceId, final Equality equality, final long resourceValue) {
		this.resourceId = resourceId;
		this.resourceValue = resourceValue;
		this.equality = equality;
	}

	@Override
	public void validate(Context context) {
		validateEquality(context, equality);
		validateResourceId(context, resourceId);
	}

	@Override
	public boolean evaluate(Context context, PersonId personId) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		if (resourceDataView == null) {
			resourceDataView = context.getDataView(ResourceDataView.class).get();
		}
		
		final long level = resourceDataView.getPersonResourceLevel(resourceId, personId);
		return equality.isCompatibleComparisonValue(Long.compare(level, resourceValue));
	}

	private Optional<PersonId> requiresRefresh(Context context, PersonResourceChangeObservationEvent event) {
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
		result.add(new FilterSensitivity<PersonResourceChangeObservationEvent>(PersonResourceChangeObservationEvent.class, this::requiresRefresh));
		return result;
	}

}
