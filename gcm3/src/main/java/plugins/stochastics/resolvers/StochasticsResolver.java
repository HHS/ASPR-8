package plugins.stochastics.resolvers;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ResolverContext;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataManager;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.events.mutation.StochasticsReseedEvent;
import plugins.stochastics.support.RandomNumberGeneratorId;
import util.SeedProvider;

/**
 * <P>
 * Creates and publishes the {@linkplain StochasticsDataView}. Initializes the
 * data views from the {@linkplain StochasticsInitialData} and a plugin provided
 * seed value.
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * <li>{@linkplain StochasticsReseedEvent} <blockquote>Resets all random number
 * generators using the same methodology used during initialization.
 * </blockquote></li>
 * </ul>
 * 
 * </P>
 * 
 * 
 * @author Shawn Hatch
 *
 */
public class StochasticsResolver {
	private final StochasticsDataManager stochasticsDataManager;

	/**
	 * Creates this resolver from the the given {@link StochasticsInitialData}
	 * 
	 */
	public StochasticsResolver(StochasticsPlugin stochasticsPlugin) {
		stochasticsDataManager = new StochasticsDataManager();

		// create RandomGenerators for each of the ids using a hash built from
		// the id and the replication seed
		Set<RandomNumberGeneratorId> randomNumberGeneratorIds = stochasticsPlugin.getRandomNumberGeneratorIds();
		long seed = stochasticsPlugin.getSeed();
		for (RandomNumberGeneratorId randomNumberGeneratorId : randomNumberGeneratorIds) {
			String name = randomNumberGeneratorId.toString();
			long seedForId = name.hashCode() + seed;
			RandomGenerator randomGeneratorForID = SeedProvider.getRandomGenerator(seedForId);
			stochasticsDataManager.setRandomGeneratorById(randomNumberGeneratorId, randomGeneratorForID);
		}
		// finally, set up the standard RandomGenerator
		stochasticsDataManager.setGeneralRandomGenerator(SeedProvider.getRandomGenerator(seed));

	}

	/**
	 * Initial behavior of this resolver.
	 * 
	 * <li>Subscribes to all handled events
	 * 
	 * <li>Publishes the {@linkplain StochasticsDataView}</li>
	 * 
	 *
	 */
	public void init(ResolverContext resolverContext) {

		resolverContext.subscribeToEventExecutionPhase(StochasticsReseedEvent.class, this::handleStochasticsReseedEvent);

		resolverContext.publishDataView(new StochasticsDataView(resolverContext.getSafeContext(), stochasticsDataManager));
	}

	private void handleStochasticsReseedEvent(ResolverContext resolverContext, StochasticsReseedEvent stochasticsReseedEvent) {

		long seed = stochasticsReseedEvent.getSeed();

		// reset the default random number generator
		stochasticsDataManager.getRandomGenerator().setSeed(seed);

		// reset the id based random number generators
		for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsDataManager.getRandomNumberGeneratorIds()) {
			String name = randomNumberGeneratorId.toString();
			long seedForId = name.hashCode() + seed;
			stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId).setSeed(seedForId);
		}

	}

}
