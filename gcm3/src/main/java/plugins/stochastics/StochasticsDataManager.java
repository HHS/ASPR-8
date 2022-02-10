package plugins.stochastics;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;
import util.SeedProvider;

/**
 * Published data manager for the Stochastics plugin.
 * 
 * @author Shawn Hatch
 *
 */

/**
 * <P>
 * Creates and publishes the {@linkplain StochasticsDataManager}. Initializes the
 * data views from the {@linkplain StochasticsInitialData} and a plugin provided
 * seed value.
 * </P>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class StochasticsDataManager extends DataManager {

	private Map<RandomNumberGeneratorId, RandomGenerator> randomGeneratorMap = new LinkedHashMap<>();

	private RandomGenerator randomGenerator;

	/**
	 * Returns the general, non-identified, random number generator was
	 * initialized with the initial seed value of the stochastics plugin
	 */
	public RandomGenerator getRandomGenerator() {
		return randomGenerator;
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
		return randomGeneratorMap.get(randomNumberGeneratorId);
	}

	/**
	 * Returns the random number generator ids that were contained in the
	 * initial data of the stochastics plugin
	 */
	@SuppressWarnings("unchecked")
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {

		Set<T> result = new LinkedHashSet<>(randomGeneratorMap.size());
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomGeneratorMap.keySet()) {
			result.add((T) randomNumberGeneratorId);
		}
		return result;

	}

	private void validateRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		RandomGenerator rng = randomGeneratorMap.get(randomNumberGeneratorId);
		if (rng == null) {
			throw new ContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}

	/**
	 * Creates the data view for the plugin
	 * 
	 */
	public StochasticsDataManager(StochasticsPluginData stochasticsPluginData) {

		// create RandomGenerators for each of the ids using a hash built from
		// the id and the replication seed
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();
		long seed = stochasticsPluginData.getSeed();
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomNumberGeneratorIds) {
			String name = randomNumberGeneratorId.toString();
			long seedForId = name.hashCode() + seed;
			RandomGenerator randomGeneratorForID = SeedProvider.getRandomGenerator(seedForId);
			this.randomGeneratorMap.put(randomNumberGeneratorId, randomGeneratorForID);			
		}
		// finally, set up the standard RandomGenerator
		randomGenerator = SeedProvider.getRandomGenerator(seed);

	}

	/**
	 * Initial behavior of this resolver.
	 * 
	 * <li>Subscribes to all handled events
	 * 
	 * <li>Publishes the {@linkplain StochasticsDataManager}</li>
	 * 
	 *
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {
		
	}

	/**
	 * Resets the seeds for all managed random number generators from the given seed.
	 */
	public void resetSeeds(long seed) {

		// reset the default random number generator
		randomGenerator.setSeed(seed);

		// reset the id based random number generators
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomGeneratorMap.keySet()) {
			String name = randomNumberGeneratorId.toString();
			long seedForId = name.hashCode() + seed;
			RandomGenerator rng = randomGeneratorMap.get(randomNumberGeneratorId);
			rng.setSeed(seedForId);
		}

	}
}
