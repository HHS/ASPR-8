package nucleus;

import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * A context provides basic access to the nucleus simulation and the data
 * managers contributed by the plugins that formed the experiment.
 * 
 * @author Shawn Hatch
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
	 * Terminates the simulation after the current plan is fully executed.
	 */
	public void halt();

	/**
	 * Returns true if an only if an actor is associated with the given id.
	 * Tolerates null values.
	 */
	public boolean actorExists(ActorId actorId);

	/**
	 * Adds an actor to the simulation. The actor is added immediately, but the
	 * consumer of ActorContext is invoked after event resolution is finished
	 * and before time progresses.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@link NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if the
	 *             actor context consumer is null
	 * 
	 */
	public ActorId addActor(Consumer<ActorContext> consumer);

	/**
	 * Removes the given actor from the simulation.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_ACTOR_ID} if the actorId is null
	 *             <li>{@link NucleusError#NEGATIVE_ACTOR_ID} if the actor id is
	 *             negative
	 *             <li>{@link NucleusError#UNKNOWN_ACTOR_ID} if the actor id
	 *             does not correspond to a known actor
	 */
	public void removeActor(ActorId actorId);
}
