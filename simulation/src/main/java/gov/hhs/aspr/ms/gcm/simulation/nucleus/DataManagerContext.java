package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * A data manager context provides access to the nucleus engine and other data
 * managers for data managers. It is supplied by the engine each time it
 * interacts with a data manager.
 */
public final class DataManagerContext {

	private final Simulation simulation;
	protected final DataManagerId dataManagerId;

	protected DataManagerContext(DataManagerId dataManagerId, Simulation simulation) {
		this.dataManagerId = dataManagerId;
		this.simulation = simulation;
	}

	/**
	 * Schedules an active data manager plan with a default arrival id that will be
	 * executed at the given time.
	 * 
	 * @throws ContractException
	 *                           <ul>                          
	 *                           <li>{@link NucleusError#NULL_PLAN_CONSUMER} if the
	 *                           consumer is null</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is added to the simulation after event
	 *                           processing is finished</li>
	 *                           </ul>
	 *                           
	 */
	public void addPlan(final Consumer<DataManagerContext> consumer, final double planTime) {
		simulation.addDataManagerPlan(dataManagerId, new DataManagerPlan(planTime, consumer));
	}

	/**
	 * Schedules a data manager plan. Plans arrival ids are ignored after the first
	 * wave of agent, report and data manager initialization during the simulation
	 * bootstrap. During the initialization phase, all plans with non-negative
	 * arrival ids (plans that were serialized) keep their arrival ids and all new
	 * plans (having arrival id = -1) are scheduled in the planning queue with
	 * higher arrival ids than all the serialized plans.
	 * 
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null</li>
	 *                           <li>{@link NucleusError#INVALID_PLAN_ARRIVAL_ID} if the
	 *                           arrival id is less than -1</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is added to the simulation after event
	 *                           processing is finished</li>
	 *                           </ul>
	 */
	public void addPlan(DataManagerPlan plan) {		
		simulation.addDataManagerPlan(dataManagerId, plan);
	}

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * Activity associated with the consumer should be limited to querying data
	 * state and releasing output.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_DATA_MANAGER_CONTEXT_CONSUMER}
	 *                           if the consumer is null
	 */
	public void subscribeToSimulationClose(Consumer<DataManagerContext> consumer) {
		simulation.subscribeDataManagerToSimulationClose(dataManagerId, consumer);
	}

	/**
	 * Returns true if and only if the data managers should output their state as a
	 * plugin data instances at the end of the simulation.
	 */
	public boolean stateRecordingIsScheduled() {
		return simulation.stateRecordingIsScheduled();
	}

	/**
	 * Returns the scheduled simulation halt time. Negative values indicate there is
	 * no scheduled halt time.
	 */
	public double getScheduledSimulationHaltTime() {
		return simulation.getScheduledSimulationHaltTime();
	}

	/**
	 * Broadcasts the given event to all subscribers. Reports handle the event
	 * immediately. Data managers and actors will have the event queued for handling
	 * after the data manager is finished with its current activation. This is used
	 * for OBSERVATION events that are generated by the data managers. MUTATION
	 * events that are generated by the data managers as a proxy for actors and data
	 * managers should use releaseMutationEvent() instead.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT} if the event is
	 *                           null
	 */
	public void releaseObservationEvent(final Event event) {
		simulation.releaseObservationEventForDataManager(event);
	}

	/**
	 * Starts the event handling process for the given event This is used for
	 * MUTATION events.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT} if the event is
	 *                           null
	 */
	public void releaseMutationEvent(final Event event) {
		simulation.releaseMutationEventForDataManager(event);
	}

	/**
	 * Subscribes the data manager to events of the given type for the purpose of
	 * execution of data changes.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_EVENT_CLASS} if the
	 *                           event class is null</li>
	 *                           <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the
	 *                           event consumer is null</li>
	 *                           <li>{@link NucleusError#DUPLICATE_EVENT_SUBSCRIPTION}
	 *                           if the data manager is already subscribed</li>
	 *                           </ul>
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		simulation.subscribeDataManagerToEvent(dataManagerId, eventClass, eventConsumer);
	}

	/**
	 * Unsubscribes the data manager from events of the given type for all phases of
	 * event handling.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT_CLASS} if the event
	 *                           class is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass) {
		simulation.unsubscribeDataManagerFromEvent(dataManagerId, eventClass);
	}

	/**
	 * Returns true if and only if there are actor or data managers subscribed to
	 * the given event type.
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
	 * consumer of ActorContext is invoked after event resolution is finished and
	 * before time progresses.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if
	 *                           the actor context consumer is null
	 */
	public ActorId addActor(Consumer<ActorContext> consumer) {
		return simulation.addActor(consumer);
	}

	public boolean actorExists(final ActorId actorId) {
		return simulation.actorExists(actorId);
	}

	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
		return simulation.getDataManagerForDataManager(dataManagerId, dataManagerClass);
	}

	public double getTime() {
		return simulation.time;
	}

	public void halt() {
		simulation.halt();
	}

	/**
	 * Removes the given actor from the simulation.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_ACTOR_ID} if the
	 *                           actorId is null</li>
	 *                           <li>{@link NucleusError#UNKNOWN_ACTOR_ID} if the
	 *                           actor id is negative</li>
	 *                           <li>{@link NucleusError#UNKNOWN_ACTOR_ID} if the
	 *                           actor id does not correspond to a known actor</li>
	 *                           </ul>
	 */
	public void removeActor(final ActorId actorId) {
		simulation.removeActor(actorId);
	}

	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}

	/**
	 * Returns the simulation time in days from the given LocalDateTime based on the
	 * LocalDate associated with simulation time=0 in the SimulationState used to
	 * initialize the simulation.
	 */
	public double getSimulationTime(LocalDateTime localDateTime) {
		return simulation.getSimulationTime(localDateTime);
	}

	/**
	 * Returns the LocalDateTime from the given simulation time based on the
	 * LocalDate associated with simulation time=0 in the SimulationState used to
	 * initialize the simulation.
	 */
	public LocalDateTime getLocalDateTime(double simulationTime) {
		return simulation.getLocalDateTime(simulationTime);
	}

	/**
	 * Returns the list of queued plans belonging to the current data manager.
	 * Should only be used after notification of simulation close.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_ACTIVE}
	 *                           if this method is invoked before the termination of
	 *                           the simulation</li>
	 *                           </ul>
	 * 
	 */
	public List<DataManagerPlan> retrievePlans() {
		return simulation.retrievePlansForDataManager(dataManagerId);
	}

	/**
	 * Returns the DataManagerId of the data manager assigned to this instance of
	 * the data manager context.
	 */
	public DataManagerId getDataManagerId() {
		return dataManagerId;
	}

}
