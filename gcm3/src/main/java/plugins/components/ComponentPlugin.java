package plugins.components;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.SimpleResolverId;
import plugins.components.resolvers.ComponentResolver;
import plugins.components.support.ComponentId;
import util.ContractError;

/**
 *
 * <p>
 * <b>Summary</b> A nucleus plugin for differentiating amongst agents by an
 * identifier other than the agent id. This provides non-numerical and more
 * natural ways to identify agents. The marker interface
 * {@linkplain ComponentId} should be used with multiple concrete implementor
 * classes that act as a way to identify an agent's type or role in the
 * simulation.
 * </p>
 *
 * <p>
 * <b>Events </b> See each event class for details.
 * <ul>
 * <li><b>ComponentConstructionEvent</b>: Constructs an agent and associates it
 * with a unique component id.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Resolvers</b>
 * <ul>
 * <li><b>ComponentEventResolver</b>: Publishes the ComponentDataView. Handles
 * all plugin-defined events.
 * </ul>
 * </p>
 *
 * <p>
 * <b>Data Views</b>
 * <ul>
 * <li><b>Component Data View</b>: Supplies component id values for agents</li> 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Reports</b> This plugin does not provide any report implementations. 
 * </p>
 *
 * <p>
 * <b>Agents: </b>This plugin does not provide any agent implementations.
 *
 * </p>
 *
 * <p>
 * <b>Initializing data:</b> This plugin has no data initialization class.
 * </ul>
 *
 *
 * </p>
 *
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>ComponentError: </b></li>Enumeration implementing
 * {@linkplain ContractError} for this plugin. * 
 * <li><b>ComponentId: </b></li>Marker interface for component id types. 
 * </ul>
 * </p>
 *
 * <p>
 * <b>Required Plugins</b> This plugin has no plugin dependencies. 
 * </p>
 *
 * @author Shawn Hatch
 *
 */

public final class ComponentPlugin {

	public final static PluginId PLUGIN_ID = new SimplePluginId(ComponentPlugin.class);

	public void init(PluginContext pluginContext) {
		pluginContext.defineResolver(new SimpleResolverId(ComponentResolver.class), new ComponentResolver()::init);
	}

}
