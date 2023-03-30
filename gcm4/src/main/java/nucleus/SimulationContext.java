package nucleus;

import util.errors.ContractException;

/**
 * A context provides basic access to the nucleus simulation and the data
 * managers contributed by the plugins that formed the experiment.
 * 
 *
 */
public interface SimulationContext {

	/**
	 * Sends output to whatever consumer of output is registered with nucleus,
	 * if any
	 */
	public void releaseOutput(Object output);

	/**
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS} if data
	 *             manager class is null</li>
	 *             <li>{@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS} if
	 *             more than one data manager matches the given class</li>
	 * 
	 */
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass);

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime();

	/**
	 * Terminates the simulation after the current time is fully executed. Has
	 * no effect if state recording is scheduled.
	 */
	public void halt();

	/**
	 * Returns true if an only if an actor is associated with the given id.
	 * Tolerates null values.
	 */
	public boolean actorExists(ActorId actorId);

}
