package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.util.errors.ContractException;

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
		result = prime * result + ((data.labels == null) ? 0 : data.labels.hashCode());
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
		if (data.labels == null) {
			if (other.data.labels != null)
				return false;
		} else if (!data.labels.equals(other.data.labels))
			return false;
		return true;
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		public LabelSet build() {
			if (!data.locked) {
				data.dimensions = Collections.unmodifiableSet(new LinkedHashSet<>(data.labels.keySet()));
				validateData();
			}
			ensureImmutability();
			return new LabelSet(data);
		}

		/**
		 * Sets the label value for the given label id. Replaces any existing label
		 * value for the same id.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PartitionError#NULL_PARTITION_LABEL_DIMENSION}
		 *                           if the dimension is null</li>
		 *                           <li>{@linkplain PartitionError#NULL_PARTITION_LABEL}
		 *                           if the label is null</li>
		 *                           </ul>
		 */
		public Builder setLabel(Object id, Object label) {
			ensureDataMutability();
			if (id == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_LABEL_DIMENSION);
			}
			if (label == null) {
				throw new ContractException(PartitionError.NULL_PARTITION_LABEL);
			}
			data.labels.put(id, label);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LabelSet [labels=");
		builder.append(data.labels);
		builder.append("]");
		return builder.toString();
	}

	private static class Data {
		private Map<Object, Object> labels = new LinkedHashMap<>();
		private Set<Object> dimensions;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			labels.putAll(data.labels);
			dimensions = Collections.unmodifiableSet(new LinkedHashSet<>(data.labels.keySet()));
			locked = data.locked;
		}
	}

	/**
	 * Returns the dimension label for this {@link LabelSet}
	 */
	public Optional<Object> getLabel(Object dimension) {
		return Optional.ofNullable(this.data.labels.get(dimension));
	}

	/**
	 * Returns an unmodifiable list of dimensions
	 */
	public Set<Object> getDimensions() {
		return data.dimensions;
	}

	/**
	 * Returns true if and only if this {@link LabelSet} has no label values
	 */
	public boolean isEmpty() {
		return data.labels.isEmpty();
	}

	private LabelSet(Data data) {
		this.data = data;
	}

	private final Data data;

	public Builder toBuilder() {
		return new Builder(data);
	}
	
}
