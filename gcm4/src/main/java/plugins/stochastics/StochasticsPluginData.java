package plugins.stochastics;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
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
			this.seed = data.seed;
			randomNumberGeneratorIds.addAll(data.randomNumberGeneratorIds);
		}

		private Long seed;
		private Set<RandomNumberGeneratorId> randomNumberGeneratorIds = new LinkedHashSet<>();
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

		private boolean dataIsMutable;

		private void ensureDataMutability() {
			if (!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}

		private Builder(Data data) {
			this.data = data;

		}

		private void validate() {
			
			if (data.seed == null) {
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
			try {
				validate();
				return new StochasticsPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds the given RandomNumberGeneratorId to this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
		 *             if the id is null</li>
		 */
		public Builder addRandomGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			ensureDataMutability();
			validateRandomNumberGeneratorIdNotNull(randomNumberGeneratorId);
			data.randomNumberGeneratorIds.add(randomNumberGeneratorId);
			return this;
		}

		/**
		 * Sets the seed value.
		 */
		public Builder setSeed(long seed) {
			ensureDataMutability();
			data.seed = seed;
			return this;
		}

	}

	private static void validateRandomNumberGeneratorIdNotNull(final Object value) {
		if (value == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
	}

	private final Data data;

	/**
	 * Returns the RandomNumberGeneratorId values contained in this container
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		Set<T> result = new LinkedHashSet<>();
		for (RandomNumberGeneratorId randomNumberGeneratorId : data.randomNumberGeneratorIds) {
			result.add((T) randomNumberGeneratorId);
		}
		return result;
	}

	/**
	 * Returns the base seed.
	 */
	public long getSeed() {
		return data.seed;
	}

	@Override
	public PluginDataBuilder getEmptyBuilder() {
		return new Builder(new Data());
	}

}
