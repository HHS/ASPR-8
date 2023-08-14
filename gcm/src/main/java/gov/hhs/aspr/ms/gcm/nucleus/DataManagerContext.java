package gov.hhs.aspr.ms.gcm.nucleus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import util.errors.ContractException;

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
	 * Schedules a plan that will be executed at the given time.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is
	 *                           scheduled for a time in the past *
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is
	 *                           added to the simulation after event processing is
	 *                           finished
	 *                           </ul>
	 */
	public void addPlan(final Consumer<DataManagerContext> consumer, final double planTime) {

		Plan<DataManagerContext> plan = Plan.builder(DataManagerContext.class)//
				.setActive(true)//
				.setCallbackConsumer(consumer)//
				.setKey(null)//
				.setPlanData(null)//
				.setTime(planTime)//
				.build();//

		simulation.addDataManagerPlan(dataManagerId, plan);
	}

	/**
	 * Schedules a plan.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is
	 *                           scheduled for a time in the past *
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is
	 *                           added to the simulation after event processing is
	 *                           finished
	 *                           </ul>
	 */
	public void addPlan(Plan<DataManagerContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
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
	 * Returns true if and only if the data managers should output their state
	 * as a plugin data instances at the end of the simulation.
	 */
	public boolean stateRecordingIsScheduled() {
		return simulation.stateRecordingIsScheduled();
	}

	/**
	 * Returns the scheduled simulation halt time. Negative values indicate
	 * there is no scheduled halt time.
	 */
	public double getScheduledSimulationHaltTime() {
		return simulation.getScheduledSimulationHaltTime();
	}

	/**
	 * Returns true if and only if there a state recording is scheduled and the
	 * given time exceeds the recording time.
	 */
	protected boolean plansRequirePlanData(double time) {
		return simulation.plansRequirePlanData(time);
	}

	/**
	 * Retrieves a plan for the given key.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_PLAN_KEY} if the plan
	 *                           key is null
	 */
	public Optional<Plan<DataManagerContext>> getPlan(final Object key) {
		return simulation.getDataManagerPlan(dataManagerId, key);
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
	 * @throws ContractException {@link NucleusError#NULL_EVENT} if the event is
	 *                           null
	 */
	public void releaseObservationEvent(final Event event) {
		simulation.releaseObservationEventForDataManager(event);
	}

	/**
	 * Starts the event handling process for the given event
	 * 
	 * This is used for MUTATION events.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT} if the event is
	 *                           null
	 */
	public void releaseMutationEvent(final Event event) {
		simulation.releaseMutationEventForDataManager(event);
	}

	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_PLAN_KEY} if the plan
	 *                           key is null
	 */
	public Optional<Plan<DataManagerContext>> removePlan(final Object key) {
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
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_EVENT_CLASS} if the
	 *                           event class
	 *                           is null
	 *                           <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the
	 *                           event
	 *                           consumer is null
	 *                           <li>{@link NucleusError#DUPLICATE_EVENT_SUBSCRIPTION}
	 *                           if the
	 *                           data manager is already subscribed
	 *                           </ul>
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<DataManagerContext, T> eventConsumer) {
		simulation.subscribeDataManagerToEvent(dataManagerId, eventClass, eventConsumer);
	}

	/**
	 * Unsubscribes the data manager from events of the given type for all
	 * phases of event handling.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT_CLASS} if the
	 *                           event class is null
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
	 * @throws ContractException {@link NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
	 *                           if the actor context consumer is null
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
	 *                           actorId is null
	 *                           <li>{@link NucleusError#NEGATIVE_ACTOR_ID} if the
	 *                           actor id is
	 *                           negative
	 *                           <li>{@link NucleusError#UNKNOWN_ACTOR_ID} if the
	 *                           actor id
	 *                           does not correspond to a known actor
	 *                           </ul>
	 */
	public void removeActor(final ActorId actorId) {
		simulation.removeActor(actorId);
	}

	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}

	/**
	 * Sets a function for converting plan data instances into consumers of
	 * actor context that will be used to convert stored plans from a previous
	 * simulation execution into current plans. Only used during the
	 * initialization of the simulation before time flows.
	 */
	public <T extends PlanData> void setPlanDataConverter(Class<T> planDataClass,
			Function<T, Consumer<DataManagerContext>> conversionFunction) {
		simulation.setDataManagerPlanDataConverter(dataManagerId, planDataClass, conversionFunction);
	}

	/**
	 * Returns the time (floating point days) of simulation start.
	 */
	public double getStartTime() {
		return simulation.getStartTime();
	}

	/**
	 * Returns the base date that synchronizes with simulation time zero.
	 */
	public LocalDate getBaseDate() {
		return simulation.getBaseDate();
	}
}
