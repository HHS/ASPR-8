package plugins.stochastics;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.DataManager;
import nucleus.util.ContractException;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.RandomGeneratorProvider;

/**
 * Published data manager for the Stochastics plugin.
 * 
 * @author Shawn Hatch
 *
 */

/**
 * <P>
 * Creates and publishes the {@linkplain StochasticsDataManager}. Initializes
 * the data views from the {@linkplain StochasticsInitialData} and a plugin
 * provided seed value.
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
		RandomGenerator result = randomGeneratorMap.get(randomNumberGeneratorId);
		if(result == null) {
			result = addRandomGenerator(randomNumberGeneratorId);
		}
		return result;
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
	}

	private long seed;

	/**
	 * Creates the data view for the plugin
	 * 
	 */
	public StochasticsDataManager(StochasticsPluginData stochasticsPluginData) {

		// create RandomGenerators for each of the ids using a hash built from
		// the id and the replication seed
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();
		seed = stochasticsPluginData.getSeed();
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomNumberGeneratorIds) {
			addRandomGenerator(randomNumberGeneratorId);
		}
		// finally, set up the standard RandomGenerator
		randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

	}
	
	/*
	 * The random generator should not already exist
	 */
	private RandomGenerator addRandomGenerator(RandomNumberGeneratorId randomNumberGeneratorId) {
		String name = randomNumberGeneratorId.toString();
		long seedForId = name.hashCode() + seed;
		RandomGenerator randomGeneratorForID = RandomGeneratorProvider.getRandomGenerator(seedForId);
		this.randomGeneratorMap.put(randomNumberGeneratorId, randomGeneratorForID);
		return randomGeneratorForID;
	}

	/**
	 * Resets the seeds for all managed random number generators from the given
	 * seed.
	 */
	public void resetSeeds(long seed) {
		this.seed = seed;

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
