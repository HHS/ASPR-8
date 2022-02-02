package plugins.stochastics.datacontainers;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.Context;
import nucleus.DataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;

/**
 * Published data view that provides random number generators. 
 * 
 * @author Shawn Hatch
 *
 */
public final class StochasticsDataView implements DataView {
	private final StochasticsDataManager stochasticsDataManager;
	private final Context context;

	/**
	 * 
	 * Constructs this data view from the given context and data manager.
	 * 
	 */
	public StochasticsDataView(Context context, StochasticsDataManager stochasticsDataManager) {
		this.context = context;
		this.stochasticsDataManager = stochasticsDataManager;
	}

	/**
	 * Returns the general, non-identified, random number generator was
	 * initialized with the initial seed value of the stochastics plugin
	 */
	public RandomGenerator getRandomGenerator() {
		return stochasticsDataManager.getRandomGenerator();
	}

	/**
	 * Returns the random generator associated with the given id. The returned
	 * random generator is seeded using the seed of the stochastics plugin and
	 * the id.
	 * 
	 * RNG seed = seed + id.toString().hashcode()
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random number generator is null</li>
	 *             <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random number generator is unknown</li>
	 */
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId) {
		validateRandomNumberGeneratorId(randomNumberGeneratorId);
		return stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
	}

	/**
	 * Returns the random number generator ids that were contained in the
	 * initial data of the stochastics plugin
	 */
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		return stochasticsDataManager.getRandomNumberGeneratorIds();
	}

	private void validateRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			context.throwContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}

		if (!stochasticsDataManager.randomGeneratorFromIdExists(randomNumberGeneratorId)) {
			context.throwContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}
}
