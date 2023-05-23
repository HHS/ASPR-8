package plugins.partitions.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of partitions. It contains: <BR>
 * <ul>
 * <li>partitions</li>
 * </ul>
 * 
 *
 */
@Immutable
public final class PartitionsPluginData implements PluginData {

	/**
	 * Builder class for PartitionsPluginData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link PartitionsPluginData} from the collected
		 * information supplied to this builder.
		 */
		public PartitionsPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PartitionsPluginData(data);
		}

		/**
		 * Set a global property Duplicate inputs override previous inputs.
		 * 
		 * @throws ContractException
		 * 
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID}</li> if
		 *             the global property id is null
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 * 
		 */
		public Builder setPartition(final Object key, final Partition partition) {
			ensureDataMutability();
			validateKeyNotNull(key);
			validatePartitionNotNull(partition);
			data.partitions.put(key, partition);
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
			// nothing to validate
		}

	}

	private static class Data {

		private final Map<Object, Partition> partitions = new LinkedHashMap<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			partitions.putAll(data.partitions);
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + partitions.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;

			/*
			 * We exclude: locked -- both should be locked when equals is
			 * invoked
			 */

			/*
			 * These are simple comparisons:
			 */
			if (!partitions.equals(other.partitions)) {
				return false;
			}

			return true;
		}

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validateKeyNotNull(Object key) {
		if (key == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_KEY);
		}
	}

	private void validateKey(Object key) {
		if (key == null) {
			throw new ContractException(PartitionError.NULL_PARTITION_KEY);
		}
		if (!data.partitions.containsKey(key)) {
			throw new ContractException(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY);
		}

	}

	private static void validatePartitionNotNull(final Partition partition) {
		if (partition == null) {
			throw new ContractException(PartitionError.NULL_PARTITION);
		}
	}

	private final Data data;

	private PartitionsPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the Partition for the given key.
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PartitionError#NULL_PARTITION_KEY}</li> if
	 *             the key is null
	 *             <li>{@linkplain PartitionError#UNKNOWN_POPULATION_PARTITION_KEY}</li>
	 *             if the key is unknown
	 */
	public Partition getPartition(Object key) {
		validateKey(key);
		return data.partitions.get(key);
	}

	/**
	 * Returns the set of partition keys
	 * 
	 */
	public Set<Object> getPartitionKeys() {
		return new LinkedHashSet<>(data.partitions.keySet());
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PartitionsPluginData)) {
			return false;
		}
		PartitionsPluginData other = (PartitionsPluginData) obj;
		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
