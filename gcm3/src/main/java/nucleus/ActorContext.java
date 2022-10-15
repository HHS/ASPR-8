package nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * An actor context provides access to the nucleus engine and published data
 * managers to actors. It is supplied by the engine each time it interacts with
 * an actor. Actors are defined by this context. If this context is passed to a
 * method invocation, then that method is an actor method.
 * 
 * @author Shawn Hatch
 *
 */

public interface ActorContext extends SimulationContext {

	/**
	 * Schedules a plan that will be executed at the given time.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 * 
	 * 
	 * 
	 */
	public void addPlan(Consumer<ActorContext> plan, double planTime);

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
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 */
	public void addKeyedPlan(Consumer<ActorContext> plan, double planTime, Object key);

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
	 * 
	 * 
	 */
	public void addPassivePlan(Consumer<ActorContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. The plan is
	 * associated with the given key and can be canceled or retrieved via this
	 * key. Keys must be unique to the actor doing the planning, but can be
	 * repeated across actors and other planning entities. Use of keys with
	 * plans should be avoided unless retrieval or cancellation is needed.
	 * Passive plans are not required to execute and the simulation will
	 * terminate if only passive plans remain on the planning schedule.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 */
	public void addPassiveKeyedPlan(Consumer<ActorContext> plan, double planTime, Object key);

	/**
	 * Retrieves a plan stored for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T extends Consumer<ActorContext>> Optional<T> getPlan(final Object key);

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
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T> Optional<T> removePlan(final Object key);

	/**
	 * Returns a list of the current plan keys associated with the current actor
	 * 
	 */
	public List<Object> getPlanKeys();

	/**
	 * Returns the ActorId of the current actor
	 */
	public ActorId getActorId();

	
	/**
	 * Subscribes the current actor to the given event filter. Events of the
	 * type T are processed by the event filter. If the event passes the filter
	 * the event will be consumed by the supplied event consumer.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_FILTER} if the event
	 *             filter is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 */
	public <T extends Event> void subscribe(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer);

	/**
	 * Subscribes the current actor to the given event.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 * 
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ActorContext, T> eventConsumer);


	/**
	 * Unsubscribes the current actor from the given event filter.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_FILTER} if the event
	 *             filter is null
	 *
	 */
	public <T extends Event> void unsubscribe(EventFilter<T> eventFilter);

	/**
	 * Unsubscribes the actor from events of the given type.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null</li>
	 */
	public <T extends Event> void unsubscribe(Class<T> eventClass);

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * Activity associated with the consumer should be limited to querying data
	 * state and releasing output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if the
	 *             consumer is null</li>
	 */
	public void subscribeToSimulationClose(Consumer<ActorContext> consumer);

	/**
	 * Returns true if and only if there are actor or data managers subscribed
	 * to the given event type.
	 */
	public boolean subscribersExist(Class<? extends Event> eventClass);
}
