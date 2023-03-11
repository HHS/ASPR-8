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
 *
 */

public final class DataManagerContext implements SimulationContext {

	private final Simulation simulation;
	protected final DataManagerId dataManagerId;

	protected DataManagerContext(DataManagerId dataManagerId, Simulation simulation) {
		this.dataManagerId = dataManagerId;
		this.simulation = simulation;
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
	 */

	public void addPlan(final Consumer<DataManagerContext> plan, final double planTime) {
		simulation.addDataManagerPlan(dataManagerId, plan, planTime, true, null);
	}

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * Activity associated with the consumer should be limited to querying data
	 * state and releasing output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_DATA_MANAGER_CONTEXT_CONSUMER}
	 *             if the consumer is null</li>
	 */
	public void subscribeToSimulationClose(Consumer<DataManagerContext> consumer) {
		simulation.subscribeDataManagerToSimulationClose(dataManagerId, consumer);
	}

	/**
	 * Registers the given consumer to be executed when the state of the
	 * simulation needs to be reflected into plugins that are released to
	 * output.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_DATA_MANAGER_STATE_CONTEXT_CONSUMER}
	 *             if the consumer is null</li>
	 */
	public void subscribeToSimulationState(BiConsumer<DataManagerContext, SimulationStateContext> consumer) {
		simulation.subscribeDataManagerToSimulationState(dataManagerId, consumer);
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
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 */
	public void addKeyedPlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
		simulation.validatePlanKeyNotNull(key);
		simulation.validateDataManagerPlanKeyNotDuplicate(dataManagerId, key);
		simulation.addDataManagerPlan(dataManagerId, plan, planTime, true, key);
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
	 */
	public void addPassivePlan(final Consumer<DataManagerContext> plan, final double planTime) {
		simulation.addDataManagerPlan(dataManagerId, plan, planTime, false, null);
	}

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
	public void addPassiveKeyedPlan(final Consumer<DataManagerContext> plan, final double planTime, final Object key) {
		simulation.validatePlanKeyNotNull(key);
		simulation.validateDataManagerPlanKeyNotDuplicate(dataManagerId, key);
		simulation.addDataManagerPlan(dataManagerId, plan, planTime, false, key);
	}

	/**
	 * Retrieves a plan for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */

	@SuppressWarnings("unchecked")

	public <T extends Consumer<DataManagerContext>> Optional<T> getPlan(final Object key) {
		return (Optional<T>) simulation.getDataManagerPlan(dataManagerId, key);
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
		return simulation.getDataManagerPlanTime(dataManagerId, key);
	}

	/**
	 * Broadcasts the given event to all subscribers. Reports handle the event
	 * immediately. Data managers and actors will have the event queued for
	 * handling after the data manager is finished with its current activation.
	 * 
	 * This is used for OBSERVATION events that are generated by the data
	 * managers. MUTATION events that are generated by the data managers as a
	 * proxy for actors and data managers should use releaseMutationEvent()
	 * instead.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT} if the event is null
	 */
	public void releaseObservationEvent(final Event event) {
		simulation.releaseObservationEventForDataManager(event);
	}

	/**
	 * Starts the event handling process for the given event
	 * 
	 * This is used for MUTATION events.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT} if the event is null
	 */
	public void releaseMutationEvent(final Event event) {
		simulation.releaseMutationEventForDataManager(event);
	}

	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T> Optional<T> removePlan(final Object key) {
		return simulation.removeDataManagerPlan(dataManagerId, key);
	}

	/**
	 * Returns a list of the current plan keys associated with the current data
	 * manager
	 */
	public List<Object> getPlanKeys() {
		return simulation.getDataManagerPlanKeys(dataManagerId);
	}

	/**
	 * Subscribes the data manager to events of the given type for the purpose
	 * of execution of data changes.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 *             <li>{@link NucleusError#DUPLICATE_EVENT_SUBSCRIPTION} if the
	 *             data manager is already subscribed
	 * 
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		simulation.subscribeDataManagerToEvent(dataManagerId, eventClass, eventConsumer);
	}

	/**
	 * Unsubscribes the data manager from events of the given type for all
	 * phases of event handling.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass) {
		simulation.unsubscribeDataManagerFromEvent(dataManagerId, eventClass);
	}

	/**
	 * Returns true if and only if there are actor or data managers subscribed
	 * to the given event type.
	 */
	public boolean subscribersExist(Class<? extends Event> eventClass) {
		return simulation.subscribersExistForEvent(eventClass);
	}

	/**
	 * Returns the ActorId of the current actor
	 */
	public ActorId getActorId() {
		return simulation.focalActorId;
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

	@Override
	public boolean actorExists(final ActorId actorId) {
		return simulation.actorExists(actorId);
	}

	@Override
	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
		return simulation.getDataManagerForDataManager(dataManagerId, dataManagerClass);
	}

	@Override
	public double getTime() {
		return simulation.time;
	}

	@Override
	public void halt() {
		simulation.halt();
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
	public void removeActor(final ActorId actorId) {
		simulation.removeActor(actorId);
	}

	@Override
	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}

}
