package nucleus;

import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * A plugin context provides plugin's the ability to add actors and data
 * managers to the initialization of each simulation instance(scenario) in an
 * experiment. It provides the set of plugin data objects gathered from the
 * plugins that compose the experiment.
 * 
 * @author Shawn Hatch
 *
 */
public final class PluginContext {
	private final Simulation simulation;
	protected PluginContext(Simulation simulation) {
		this.simulation = simulation;
	}

	/**
	 * 
	 * Adds a data manager to the simulation.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
	 *             plugin initialization is over</li>
	 * 
	 */	
	public void addDataManager(DataManager dataManager) {
		simulation.addDataManager(dataManager);
	}

	/**
	 * 
	 * Adds an actor to the simulation.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
	 *             plugin initialization is over</li>
	 * 
	 */
	public ActorId addActor(Consumer<ActorContext> init) {
		return simulation.addActorForPlugin(init);
	}

	/**
	 * Returns the plugin data object associated with the given class reference
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS} if
	 *             the class reference is null</li>
 
	 *             <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_CLASS} if
	 *             more than one plugin data object matches the class
	 *             reference</li>
	 * 
	 *             <li>{@linkplain NucleusError#UNKNOWN_PLUGIN_DATA_CLASS} if no
	 *             plugin data object matches the class reference</li> 
	 * 
	 */
	public <T extends PluginData> T getPluginData(Class<T> pluginDataClass) {
		return simulation.getPluginData(pluginDataClass);
	}
}
