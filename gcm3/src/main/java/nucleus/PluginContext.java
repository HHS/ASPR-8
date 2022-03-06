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
 * 3)create actors, 4) generate initial events(mutations to data views), 5)
 * register for event observation and 6)schedule future plans. Time moves
 * forward via planning and the simulation halts once all plans are complete.
 * 
 * @author Shawn Hatch
 *
 */
public interface PluginContext {

	/**
	 * 
	 * Adds a data manager to the simulation.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED} if
	 *             plugin initialization is over	 *             
	 *             
	 */

	public void addDataManager(DataManager dataManager);

	public ActorId addActor(Consumer<ActorContext> init);

	public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass);
}
