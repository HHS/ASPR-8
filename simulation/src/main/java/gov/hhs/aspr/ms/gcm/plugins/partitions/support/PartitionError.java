package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PartitionError implements ContractError {

	PAST_LABEL_FAILURE("A labeler has failed to properly identify a person's past label from an event"),//
	DUPLICATE_PARTITION("Duplicate partition key"),//
	NON_DEGENERATE_PARTITION("Requires a degenerate partition"),//
	INCOMPATIBLE_LABEL_SET("The label set is incompatible with the selected population partition definition"),
	MALFORMED_PARTITION_SAMPLE_WEIGHTING_FUNCTION(
			"Data used to form an enumerated distribution for partition sampling was malformed"),//
	NULL_LABEL_SET_FUNCTION("Null labelset function"),
	RESERVED_PARTITION_TRIGGER(
			"An event class is being used to trigger refreshes as part of a partition that is reserved for the partition resolver"),//
	NON_COMPARABLE_ATTRIBUTE("The attribute definition is not compatible with innequality comparisons"),//
	NULL_EQUALITY_OPERATOR("Null equality operator"),//
	NULL_LABEL_SET("Null label set"),//
	NULL_PARTITION("Null partition"),//
	NULL_PARTITION_PLUGIN("null partitions plugin"),//
	NULL_PARTITION_PLUGIN_DATA("null partitions plugin data"),//
	NULL_PARTITION_LABEL("Null partition label"),//
	NULL_PARTITION_LABEL_DIMENSION("Null partition label dimension"),//
	NULL_FILTER("Null filter"),//
	NULL_PARTITION_KEY("Null population partition key"),//
	NULL_POPULATION_PARTITION("Null population partition"),//	
	NULL_PARTITION_DATA_MANAGER("Null partition data manager"),//
	NULL_PARTITION_SAMPLER("Null partition sampler"),//
	UNKNOWN_POPULATION_PARTITION_KEY("No population partition found"),//
	NULL_PERSON_DATA_VIEW("Null person data view");

	private final String description;

	private PartitionError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
