package gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.Well;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.errors.ContractException;

/**
 * A mutable data manager for random number generators.
 * 
 */
public final class StochasticsDataManager extends DataManager {

	private Map<RandomNumberGeneratorId, Well> randomGeneratorMap = new LinkedHashMap<>();

	private Well randomGenerator;

	/**
	 * Returns the general, non-identified, random number generator was
	 * initialized with the current base seed value that was initialized from
	 * the {@linkplain StochasticsPluginData} or reset via
	 * {@linkplain StochasticsDataManager#resetSeeds(long)}
	 */
	public Well getRandomGenerator() {
		return randomGenerator;
	}

	/**
	 * Returns the random generator associated with the given id.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random number generator is null</li>
	 *             <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random number generator is unknown</li>
	 */
	public Well getRandomGeneratorFromId(RandomNumberGeneratorId randomNumberGeneratorId) {
		validateRandomNumberGeneratorId(randomNumberGeneratorId);
		return randomGeneratorMap.get(randomNumberGeneratorId);
	}

	/**
	 * Returns the random number generator ids.
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
	 * Returns true if and only if the random generator id is known
	 */
	public boolean randomNumberGeneratorIdExists(RandomNumberGeneratorId randomNumberGeneratorId) {
		return randomGeneratorMap.containsKey(randomNumberGeneratorId);
	}

	/**
	 * Adds a new RNG.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain StochasticsError#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the randomNumberGeneratorId is null</li>
	 *             <li>{@linkplain StochasticsError#RANDOM_NUMBER_GENERATOR_ID_ALREADY_EXISTS}
	 *             if the randomNumberGeneratorId was previously added</li>
	 *             <li>{@linkplain StochasticsError#NULL_WELL_STATE} if the
	 *             wellstate is null</li>
	 */
	public RandomGenerator addRandomNumberGenerator(RandomNumberGeneratorId randomNumberGeneratorId, WellState wellState) {
		validateNewRandomNumberGeneratorId(randomNumberGeneratorId);
		validateWellStateNotNull(wellState);
		Well result = new Well(wellState);
		randomGeneratorMap.put(randomNumberGeneratorId, result);
		return result;
	}

	private void validateRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		if (!randomGeneratorMap.containsKey(randomNumberGeneratorId)) {
			throw new ContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID);
		}
	}

	private void validateWellStateNotNull(WellState wellState) {
		if (wellState == null) {
			throw new ContractException(StochasticsError.NULL_WELL_STATE);
		}
	}

	private void validateNewRandomNumberGeneratorId(RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			throw new ContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		if (randomGeneratorMap.containsKey(randomNumberGeneratorId)) {
			throw new ContractException(StochasticsError.RANDOM_NUMBER_GENERATOR_ID_ALREADY_EXISTS);
		}
	}

	/**
	 * Creates the StochasticsDataManager from the given
	 * {@linkplain StochasticsPluginData} Random generators associated with
	 * predefined ids in the StochasticsPluginData are generated in the same
	 * manner as the method
	 * {@linkplain StochasticsDataManager#getRandomGeneratorFromId(RandomNumberGeneratorId)}
	 */
	public StochasticsDataManager(StochasticsPluginData stochasticsPluginData) {

		// create RandomGenerators for each of the ids using a hash built from
		// the id and the replication seed
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();
		randomGenerator = new Well(stochasticsPluginData.getWellState());		
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomNumberGeneratorIds) {
			WellState wellState = stochasticsPluginData.getWellState(randomNumberGeneratorId);
			randomGeneratorMap.put(randomNumberGeneratorId, new Well(wellState));
		}

		// finally, set up the standard RandomGenerator
		randomGenerator = new Well(stochasticsPluginData.getWellState());
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {
		StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomGeneratorMap.keySet()) {
			Well wellRNG = randomGeneratorMap.get(randomNumberGeneratorId);
			builder.addRNG(randomNumberGeneratorId, wellRNG.getWellState());
		}
		builder.setMainRNGState(randomGenerator.getWellState());		
		dataManagerContext.releaseOutput(builder.build());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StochasticsDataManager [randomGeneratorMap=");
		builder.append(randomGeneratorMap);
		builder.append(", randomGenerator=");
		builder.append(randomGenerator);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
