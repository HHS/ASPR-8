package plugins.stochastics.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;

/**
 * /** Mutable data manager that backs the {@linkplain StochasticsDataView}.
 * This data manager is for internal use by the {@link StochasticsPlugin} and
 * should not be published. 
 * 
 * @author Shawn Hatch
 *
 */
public final class StochasticsDataManager {
	private Map<RandomNumberGeneratorId, RandomGenerator> randomGeneratorMap = new LinkedHashMap<>();

	private RandomGenerator randomGenerator;
	
	/**
	 * Sets the general random generator.
	 */
	public void setGeneralRandomGenerator(RandomGenerator randomGenerator) {
		if(randomGenerator == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR);
		}
		this.randomGenerator = randomGenerator;
	}
	
	/**
	 * Sets the general random generator.
	 */
	public void setRandomGeneratorById(RandomNumberGeneratorId randomNumberGeneratorId,RandomGenerator randomGenerator) {
		if(randomGenerator == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR);
		}
		
		if(randomNumberGeneratorId == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		randomGeneratorMap.put(randomNumberGeneratorId, randomGenerator);
	}


	/**
	 * Returns the random number generator ids that were contained in the
	 * initial data that formed this data manager
	 */
	@SuppressWarnings("unchecked")
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		Set<T> result = new LinkedHashSet<>(randomGeneratorMap.size());
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomGeneratorMap.keySet()) {
			result.add((T) randomNumberGeneratorId);
		}
		return result;
	}

	/**
	 * Returns the general, non-identified, random number generator was
	 * initialized with the seed value presented to the constructor of this
	 * manager.
	 */
	public RandomGenerator getRandomGenerator() {
		return randomGenerator;
	}

	/**
	 * Returns true if and only if the given random number generator id is
	 * contained in the initial data presented to this data manager.
	 */
	public boolean randomGeneratorFromIdExists(RandomNumberGeneratorId randomNumberGeneratorId) {
		return randomGeneratorMap.containsKey(randomNumberGeneratorId);
	}

	/**
	 * Returns the random generator associated with the given id. Returns null
	 * if the id is not in the set of ids present in the initial data presented
	 * to this data manager. The returned random generator is seeded using the
	 * seed given to the manager's constructor and the id.
	 * 
	 * RNG seed = seed + id.toString().hashcode()
	 */
	public RandomGenerator getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId) {
		return randomGeneratorMap.get(randomNumberGeneratorId);
	}
}
