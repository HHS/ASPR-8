package nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * A data manager context provides access to the nucleus engine and other data
 * managers for data managers. It is supplied by the engine each time it
 * interacts with a data manager.
 * 
 * @author Shawn Hatch
 *
 */

public interface DataManagerContext extends SimulationContext {

	/**
	 * Schedules a plan that will be executed at the given time.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 * 
	 */
	public void addPlan(Consumer<DataManagerContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. The plan is
	 * associated with the given key and can be canceled or retrieved via this
	 * key. Keys must be unique to the actor doing the planning, but can be
	 * repeated across actors and other planning entities. Use of keys with
	 * plans should be avoided unless retrieval or cancellation is needed.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 */
	public void addKeyedPlan(Consumer<DataManagerContext> plan, double planTime, Object key);

	/**
	 * Schedules a plan that will be executed at the given time. Passive plans
	 * are not required to execute and the simulation will terminate if only
	 * passive plans remain on the planning schedule.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 * 
	 */
	public void addPassivePlan(Consumer<DataManagerContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. The plan is
	 * associated with the given key and can be canceled or retrieved via this
	 * key. Keys must be unique to the actor doing the planning, but can be
	 * repeated across actors and other planning entities. Use of keys with
	 * plans should be avoided unless retrieval or cancellation is
	 * needed.Passive plans are not required to execute and the simulation will
	 * terminate if only passive plans remain on the planning schedule.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 */
	public void addPassiveKeyedPlan(Consumer<DataManagerContext> plan, double planTime, Object key);

	/**
	 * Retrieves a plan for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T extends Consumer<DataManagerContext>> Optional<T> getPlan(final Object key);

	/**
	 * Returns the scheduled execution time for the plan associated with the
	 * given key
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public Optional<Double> getPlanTime(final Object key);

	/**
	 * Broadcasts the given event to all subscribers. Data manager subscribers
	 * receive events immediately. Actors receive events after the current actor
	 * or data manager has completed its current actions.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT} if the event is null
	 */
	public void releaseEvent(final Event event);

	
	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T> Optional<T> removePlan(final Object key);

	/**
	 * Returns a list of the current plan keys associated with the current data
	 * manager
	 */
	public List<Object> getPlanKeys();

	/**
	 * Subscribes the data manager to events of the given type for the purpose
	 * of execution of data changes.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer);

	/**
	 * Unsubscribes the data manager from events of the given type for all
	 * phases of event handling.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass);

	/**
	 * Returns true if and only if there are actor or data managers subscribed
	 * to the given event type.
	 */
	public boolean subscribersExist(Class<? extends Event> eventClass);

	/**
	 * Returns the DataManagerId associated with this DataManagerContext
	 */
	public DataManagerId getDataManagerId();

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * Activity associated with the consumer should be limited to querying data
	 * state and releasing output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_DATA_MANAGER_CONTEXT_CONSUMER} if the
	 *             consumer is null</li> 
	 */
	public void subscribeToSimulationClose(Consumer<DataManagerContext> consumer);

}
