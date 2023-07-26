package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

/**
 * A functional interface for selecting people from a {@link Partition} based on
 * assigning a weighting value to a {@link LabelSet}.
 * 
 *
 */
public interface LabelSetFunction<T> {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * This function should be stable: repeated invocations with the same
	 * arguments should return the same value during the span of a single sample
	 * of a {@link Partition}.
	 */
	public T getValue(PartitionsContext partitionsContext, LabelSet labelSet);
}
