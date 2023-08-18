package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import util.errors.ContractException;

/**
 * A {@linkplain LabelSet} is a set of labels that are used to specify a sub-set
 * of the cell space of a partition during sampling. Partitions are composed of
 * cells that are associated with combinations of labels associated with the
 * various attributes of people. The label set specifies a subset of that space
 * by value. For example: Suppose a partition is formed by the regions and two
 * person properties. The regions are grouped together under state labels. The
 * first property is the Integer AGE and is grouped by PRESCHOOL, SCHOOL and
 * ADULT. The second property is the Integer VACCINE_DOSES_RECEIVED and ranges
 * from 0 to 3 inclusive. The {@link LabelSet} [REGION = VIRGINIA, AGE=PRESHOOL,
 * VACCINE_DOSES_RECEIVED=2] will match the single partition cell that
 * represents Virginia preschoolers who have received 2 doses of vaccine. The
 * {@link LabelSet} [REGION = VIRGINIA, AGE=PRESHOOL] will match all partition
 * cells that represent Virginia preschoolers, without regard to doses of
 * vaccine received. Label Sets are built by the modeler via the supplied
 * Builder class.
 */

public final class LabelSet {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabelSet other = (LabelSet) obj;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		return true;
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Builder() {

		}

		private Data data = new Data();

		public LabelSet build() {
			return new LabelSet(new Data(data));
		}

		/**
		 * Sets the label value for the given label id. Replaces any existing label
		 * value for the same id.
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PartitionError#NULL_PARTITION_LABEL_DIMENSION}
		 *                           if the dimension is null</li>
		 *                           <li>{@linkplain PartitionError#NULL_PARTITION_LABEL}
		 *                           if the label is null</li>
		 *                           </ul>
		 */
		public Builder setLabel(Object id, Object label) {
			if (id == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_LABEL_DIMENSION);
			}
			if (label == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_LABEL);
			}
			data.labels.put(id, label);
			return this;
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LabelSet [labels=");
		builder.append(labels);
		builder.append("]");
		return builder.toString();
	}

	private static class Data {
		private Map<Object, Object> labels = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data data) {
			labels.putAll(data.labels);
		}
	}

	/**
	 * Returns the dimension label for this {@link LabelSet}
	 */
	public Optional<Object> getLabel(Object dimension) {
		return Optional.ofNullable(this.labels.get(dimension));
	}

	/**
	 * Returns an unmodifiable list of dimensions
	 */
	public Set<Object> getDimensions() {
		return dimensions;
	}

	/**
	 * Returns true if and only if this {@link LabelSet} has no label values
	 */
	public boolean isEmpty() {
		return labels.isEmpty();
	}

	private LabelSet(Data data) {
		this.labels = data.labels;
		this.dimensions = Collections.unmodifiableSet(new LinkedHashSet<>(labels.keySet()));
	}

	private final Map<Object, Object> labels;
	private final Set<Object> dimensions;
}
