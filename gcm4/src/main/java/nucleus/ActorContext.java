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
 *
 */

public final class ActorContext implements SimulationContext {
	
	private final Simulation simulation;

	protected ActorContext(Simulation simulation) {
		this.simulation = simulation;
	}
	
	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * Activity associated with the consumer should be limited to querying data
	 * state and releasing output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if the
	 *             consumer is null</li>
	 */
	
	public void subscribeToSimulationClose(Consumer<ActorContext> consumer) {
		simulation.subscribeActorToSimulationClose(consumer);
	}

	/**
	 * Registers the given consumer to be executed when the state of the
	 * simulation needs to be reflected into plugins that are released to
	 * output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REPORT_STATE_CONTEXT_CONSUMER}
	 *             if the consumer is null</li>
	 */
	public void subscribeToSimulationState(BiConsumer<ActorContext, SimulationStateContext> consumer) {
		simulation.subscribeActorToSimulationState(consumer);
	}
	
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
	
	public void addPlan(final Consumer<ActorContext> plan, final double planTime) {
		simulation.addActorPlan(plan, planTime, true, null);
	}
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
	public void addKeyedPlan(final Consumer<ActorContext> plan, final double planTime, final Object key) {
		simulation.validatePlanKeyNotNull(key);
		simulation.validateActorPlanKeyNotDuplicate(key);
		simulation.addActorPlan(plan, planTime, true, key);
	}

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
	public void addPassivePlan(final Consumer<ActorContext> plan, final double planTime) {
		simulation.addActorPlan(plan, planTime, false, null);
	}
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
	public void addPassiveKeyedPlan(final Consumer<ActorContext> plan, final double planTime, final Object key) {
		simulation.validatePlanKeyNotNull(key);
		simulation.validateActorPlanKeyNotDuplicate(key);
		simulation.addActorPlan(plan, planTime, false, key);
	}
	/**
	 * Retrieves a plan stored for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	
	@SuppressWarnings("unchecked")	
	public <T extends Consumer<ActorContext>> Optional<T> getPlan(final Object key) {
		return (Optional<T>) simulation.getActorPlan(key);
	}
	/**
	 * Returns the scheduled execution time for the plan associated with the
	 * given key
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public Optional<Double> getPlanTime(final Object key) {
		return simulation.getActorPlanTime(key);
	}

	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	
	public <T extends Consumer<ActorContext>> Optional<T> removePlan(final Object key) {
		return simulation.removeActorPlan(key);
	}

	/**
	 * Returns a list of the current plan keys associated with the current actor
	 * 
	 */	
	public List<Object> getPlanKeys() {
		return simulation.getActorPlanKeys();
	}

	/**
	 * Returns the ActorId of the current actor
	 */	
	public ActorId getActorId() {
		return simulation.focalActorId;
	}

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
	public <T extends Event> void subscribe(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer) {
		simulation.subscribeActorToEventByFilter(eventFilter, eventConsumer);
	}
	/**
	 * Unsubscribes the current actor from the given event filter.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_FILTER} if the event
	 *             filter is null
	 *
	 */	
	public <T extends Event> void unsubscribe(EventFilter<T> eventFilter) {
		simulation.unsubscribeActorFromEventByFilter(eventFilter);
	}
	
	
	@Override
	public boolean actorExists(final ActorId actorId) {
		return simulation.actorExists(actorId);
	}
	
	@Override
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
		return simulation.getDataManagerForActor(dataManagerClass);
	}
	
	@Override
	public double getTime() {
		return simulation.time;
	}
	
	@Override
	public void halt() {
		simulation.halt();
	}
	
	@Override
	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}
	
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
	public ActorId addActor(Consumer<ActorContext> consumer) {
		return simulation.addActor(consumer);
	}

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
	public void removeActor(ActorId actorId) {
		simulation.removeActor(actorId);
	}
	
	
	
	
}
