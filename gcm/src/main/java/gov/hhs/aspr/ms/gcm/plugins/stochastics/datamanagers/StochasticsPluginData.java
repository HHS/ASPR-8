package gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A thread-safe container for the initial state of random generators.
 *
 *
 */
@ThreadSafe
public final class StochasticsPluginData implements PluginData {

	/**
	 * Constructs this plugin from the initial data and seed
	 */
	private StochasticsPluginData(Data data) {
		this.data = data;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	/*
	 * State container class for collecting random number generator ids.
	 */
	private static class Data {
		public Data() {
		}

		public Data(Data data) {
			this.wellState = data.wellState;
			randomNumberGeneratorIds.putAll(data.randomNumberGeneratorIds);
			locked = data.locked;
		}

		private WellState wellState;
		private Map<RandomNumberGeneratorId, WellState> randomNumberGeneratorIds = new LinkedHashMap<>();
		private boolean locked;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + randomNumberGeneratorIds.hashCode();
			result = prime * result + wellState.hashCode();
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
			if (randomNumberGeneratorIds == null) {
				if (other.randomNumberGeneratorIds != null) {
					return false;
				}
			} else if (!randomNumberGeneratorIds.equals(other.randomNumberGeneratorIds)) {
				return false;
			}
			if (wellState == null) {
				if (other.wellState != null) {
					return false;
				}
			} else if (!wellState.equals(other.wellState)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [wellState=");
			builder.append(wellState);
			builder.append(", randomNumberGeneratorIds=");
			builder.append(randomNumberGeneratorIds);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for StochasticsPluginData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

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

		private Builder(Data data) {
			this.data = data;

		}

		private void validateData() {
			if (data.wellState == null) {
				throw new ContractException(StochasticsError.NULL_SEED);
			}
		}

		/**
		 * Returns the StochasticsInitialData formed from the collected
		 * RandomNumberGeneratorId values. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain StochasticsError#NULL_SEED} if the seed
		 *             was not set</li>
		 * 
		 */
		public StochasticsPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new StochasticsPluginData(data);

		}

		/**
		 * Adds the given RandomNumberGeneratorId to this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
		 *             if the id is null</li>
		 * 
		 *             <li>{@linkplain StochasticsError#NULL_WELL_STATE} if the
		 *             well state is null</li>
		 */
		public Builder addRNG(RandomNumberGeneratorId randomNumberGeneratorId, WellState wellState) {
			ensureDataMutability();
			validateRandomNumberGeneratorIdNotNull(randomNumberGeneratorId);
			validateWellStateNull(wellState);
			data.randomNumberGeneratorIds.put(randomNumberGeneratorId, wellState);
			return this;
		}

		/**
		 * Sets the seed value.
		 */
		public Builder setMainRNGState(WellState wellState) {
			ensureDataMutability();
			data.wellState = wellState;
			return this;
		}

	}

	private static void validateRandomNumberGeneratorIdNotNull(final Object value) {
		if (value == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
	}

	private static void validateWellStateNull(final WellState wellState) {
		if (wellState == null) {
			throw new ContractException(StochasticsError.NULL_WELL_STATE);
		}
	}

	private final Data data;

	/**
	 * Returns the RandomNumberGeneratorId values contained in this container
	 */
	@SuppressWarnings("unchecked")
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		Set<T> result = new LinkedHashSet<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : data.randomNumberGeneratorIds.keySet()) {
			result.add((T) randomNumberGeneratorId);
		}
		return result;
	}

	/**
	 * Returns the well state for the give RandomNumberGeneratorId
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the randomNumberGeneratorId is not known</li>
	 */
	public WellState getWellState(RandomNumberGeneratorId randomNumberGeneratorId) {

		WellState result = data.randomNumberGeneratorIds.get(randomNumberGeneratorId);
		if (result == null) {
			throw new ContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID);
		}
		return result;
	}

	/**
	 * Returns the main well state
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the randomNumberGeneratorId is not known</li>
	 */
	public WellState getWellState() {
		return data.wellState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StochasticsPluginData)) {
			return false;
		}
		StochasticsPluginData other = (StochasticsPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("StochasticsPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}
