package plugins.stochastics;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.stochastics.initialdata.StochasticsInitialData;
import plugins.stochastics.resolvers.StochasticsResolver;
import util.ContractError;

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

public final class StochasticsPlugin {
	
	public final static PluginId PLUGIN_ID = new SimplePluginId(StochasticsPlugin.class);
	
	private final StochasticsInitialData stochasticsInitialData;

	/**
	 * Constructs this plugin from the initial data and seed
	 */
	public StochasticsPlugin(StochasticsInitialData stochasticsInitialData) {
		this.stochasticsInitialData = stochasticsInitialData;
	}

	/**
	 * Initial behavior of this plugin. <BR>
	 *
	 * <UL>
	 * <li>defines the single resolver {@linkplain StochasticsResolver}</li>
	 * </UL>
	 */

	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(StochasticsResolver.class), new StochasticsResolver(stochasticsInitialData)::init);
	}
}
