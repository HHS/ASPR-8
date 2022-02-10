package plugins.stochastics;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginInitializer;
import nucleus.PluginContext;
import nucleus.PluginId;
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
@ThreadSafe
public final class StochasticsPluginBehavior implements PluginInitializer{
	
	
	@Override
	public PluginId getPluginId() {
		return StochasticsPluginId.PLUGIN_ID;
	}

	@Override
	public void init(PluginContext pluginContext) {
		StochasticsPluginData stochasticsPluginData = pluginContext.getPluginData(StochasticsPluginData.class).get();
		pluginContext.addDataManager(new StochasticsDataManager(stochasticsPluginData));
		
	}
}
