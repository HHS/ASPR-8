package plugins.stochastics.initialdata;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.DataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;

/**
 * An immutable container of the initial state of the stochastic plugin. It
 * contains: <BR>
 * <ul>
 * <li>base seed value for all random generators</li>
 * <li>random generator ids</li>
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
public class StochasticsInitialData implements DataView {

	/*
	 * State container class for collecting random number generator ids.
	 */
	private static class Data {
		private Long seed;
		private Set<RandomNumberGeneratorId> randomNumberGeneratorIds = new LinkedHashSet<>();
	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for StochasticsInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}
		private void validate() {
			if(data.seed == null) {
				throw new ContractException(StochasticsError.NULL_SEED);
			}
		}
		/**
		 * Returns the StochasticsInitialData formed from the collected
		 * RandomNumberGeneratorId values. Clears the builder's state.
		 * 
		 * @throws ContractException
		 * <li>{@linkplain StochasticsError#NULL_SEED} if the seed was not set</li>
		 * 
		 */
		public StochasticsInitialData build() {
			try {
				validate();
				return new StochasticsInitialData(data);
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
		 *             <li>{@linkplain StochasticsError#DUPLICATE_RANDOM_NUMBER_GENERATOR_ID}
		 *             if the id was previously added</li>
		 */
		public Builder addRandomGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
			validateRandomNumberGeneratorIdNotNull(randomNumberGeneratorId);
			validateRandomNumberGeneratorIdDoesNotExist(data, randomNumberGeneratorId);
			data.randomNumberGeneratorIds.add(randomNumberGeneratorId);
			return this;
		}
		
		/**
		 * Sets the seed value.
		 */
		public Builder setSeed(long seed) {
			data.seed = seed;
			return this;
		}
		
		
	}
	
	

	private static void validateRandomNumberGeneratorIdNotNull(final Object value) {
		if (value == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
	}

	private static void validateRandomNumberGeneratorIdDoesNotExist(final Data data, final RandomNumberGeneratorId randomNumberGeneratorId) {

		if (data.randomNumberGeneratorIds.contains(randomNumberGeneratorId)) {
			throw new ContractException(StochasticsError.DUPLICATE_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}

	private final Data data;

	private StochasticsInitialData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the RandomNumberGeneratorId values contained in this {@link StochasticsInitialData}
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
	 * Returns the seed.
	 */	
	public long getSeed() {
		return data.seed;
	}

}
