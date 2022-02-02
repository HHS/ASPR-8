package plugins.stochastics;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.Plugin;
import nucleus.PluginBuilder;
import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.stochastics.resolvers.StochasticsResolver;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractError;
import util.ContractException;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for managing random number generators. The
 * plugin provides a general random generator as well as fixed set of random
 * generators mapped to a set of identifiers provided as initialization data.
 * All random generators are implemented by
 * org.apache.commons.math3.random.Well44497b
 * </p>
 *
 * <p>
 * <b>Events </b> The plugin supports no events.
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>StochasticsEventResolver</b>: Initializes and publishes the
 * stochastics data view
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b> Supplies a single data view
 * <ul>
 * <li><b>Stochastics Data View</b>: Supplies random generators</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> The plugin defines no reports
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> An immutable container of the initial state of
 * random generator id values.
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>StochasticsError: </b></li>Enumeration implementing
 * {@linkplain ContractError} for this plugin.
 * <li><b>RandomNumberGeneratorId: </b></li>Marker interface for generator id
 * values
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b> This plugin has no plugin dependencies
 * </p>
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class StochasticsPlugin implements Plugin {
	
	public final static PluginId PLUGIN_ID = new SimplePluginId(StochasticsPlugin.class);
	
	

	/**
	 * Constructs this plugin from the initial data and seed
	 */
	private StochasticsPlugin(Data data) {
		this.data = data;
	}

	/**
	 * Initial behavior of this plugin. <BR>
	 *
	 * <UL>
	 * <li>defines the single resolver {@linkplain StochasticsResolver}</li>
	 * </UL>
	 */

	@Override
	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(StochasticsResolver.class), new StochasticsResolver(this)::init);
	}

	@Override
	public PluginId getPluginId() {		
		return PLUGIN_ID;
	}

	@Override
	public PluginBuilder getCloneBuilder() {
		return new Builder(new Data(data));
	}
	
	/*
	 * State container class for collecting random number generator ids.
	 */
	private static class Data {
		public Data() {}
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
	 * Builder class for StochasticsInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder implements PluginBuilder{
		private Data data;

		private Builder(Data data) {
			this.data = data;

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
		public StochasticsPlugin build() {
			try {
				validate();
				return new StochasticsPlugin(data);
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
