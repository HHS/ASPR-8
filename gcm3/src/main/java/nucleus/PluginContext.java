package nucleus;

import java.util.Optional;
import java.util.function.Consumer;

import util.ContractException;

/**
 * A plugin represents a suite of software delivering a set of generally related
 * simulation capabilities. 
 * 
 * Plugins are loaded into the nucleus engine and organized based upon their
 * dependency requirements. Each plugin contributes zero to many initialization
 * behaviors that 1) start the simulation, 2)initialize and publish data views,
 * 3)create agents, 4) generate initial events(mutations to data views), 5)
 * register for event observation and 6)schedule future plans. Time moves
 * forward via planning and the simulation halts once all plans are complete.
 * 
 * @author Shawn Hatch
 *
 */
public interface PluginContext {
	/**
	 * Establishes that the plugin using this plugin context depends upon the
	 * given plugin.
	 * 
	 * Plugin dependencies are gathered by nucleus and used to determine that
	 * the simulation is well formed. Nucleus requires that: 1) there are no
	 * duplicate plugins, 2)there are no null plugins, 3)there are no missing
	 * plugins, and 4) the plugin dependencies form an acyclic, directed graph.
	 * 
	 * Nucleus will initialize each plugin primarily in the order dictated by
	 * this graph and secondarily in the order each plugin was contributed to
	 * nucleus.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
	 *             plugin initialization is over
	 *             <li>{@link NucleusError#NULL_PLUGIN_ID} if the plugin id is
	 *             null
	 */
	public void addPluginDependency(PluginId pluginId);

	/**
	 * Defines the initialization behavior for event resolvers contributed by
	 * this plugin using this plugin context. At the start of the simulation,
	 * nucleus determines the order to initialize each plugin. For each plugin,
	 * nucleus invokes the consumers of ResolverContext supplied here in the
	 * order the were added by the plugin.
	 * 
	 * Resolvers use this initialization invocation to 1) initialize and publish
	 * data views, 2) create initial agents and 3) schedule plans.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
	 *             plugin initialization is over	 *             
	 *             
	 */

	public void addDataManager(DataManager dataManager);

	public AgentId addAgent(Consumer<AgentContext> init);

	public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass);
}
