package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PartitionError implements ContractError {

	DUPLICATE_PARTITION("Duplicate partition key"),//
	INCOMPATIBLE_LABEL_SET("The label set is incompatible with the selected population partition definition"),//
	MALFORMED_PARTITION_SAMPLE_WEIGHTING_FUNCTION("Data used to form an enumerated distribution for partition sampling was malformed"),//
	NON_COMPARABLE_ATTRIBUTE("The attribute definition is not compatible with innequality comparisons"),//
	NON_DEGENERATE_PARTITION("Requires a degenerate partition"),//
	NULL_EQUALITY_OPERATOR("Null equality operator"),//
	NULL_FILTER("Null filter"),//
	NULL_LABEL_SET("Null label set"),//
	NULL_LABEL_SET_FUNCTION("Null labelset function"),
	NULL_PARTITION("Null partition"),//
	NULL_PARTITION_KEY("Null population partition key"),//
	NULL_PARTITION_LABEL("Null partition label"),//
	NULL_PARTITION_LABEL_DIMENSION("Null partition label dimension"),//
	NULL_PARTITION_PLUGIN("null partitions plugin"),//
	NULL_PARTITION_PLUGIN_DATA("null partitions plugin data"),//
	NULL_PARTITION_SAMPLER("Null partition sampler"),//
	NULL_PEOPLE_DATA_MANAGER("Null person data view"),//
	PAST_LABEL_FAILURE("A labeler has failed to properly identify a person's past label from an event"),//
	RESERVED_PARTITION_TRIGGER("An event class is being used to trigger refreshes as part of a partition that is reserved for the partition resolver"),//
	UNKNOWN_POPULATION_PARTITION_KEY("No population partition found"),//
	;

	private final String description;

	private PartitionError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
