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

public final class ReportContext {
	
	private final Simulation simulation;

	protected ReportContext(Simulation simulation) {
		this.simulation = simulation;
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
	public void addPassivePlan(final Consumer<ReportContext> plan, final double planTime) {
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
	public void addPassiveKeyedPlan(final Consumer<ReportContext> plan, final double planTime, final Object key) {
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
	public <T extends Consumer<ReportContext>> Optional<T> getPlan(final Object key) {
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
	
	public <T extends Consumer<ReportContext>> Optional<T> removePlan(final Object key) {
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
	 * Subscribes the report to events of the given type for the purpose
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
	public <T extends Event> void subscribe(Class<T> eventClass,
			BiConsumer<DataManagerContext, T> eventConsumer) {
		simulation.subscribeDataManagerToEvent(dataManagerId, eventClass,
				eventConsumer);
	}

	/**
	 * Unsubscribes the report from events of the given type for all
	 * phases of event handling.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass) {
		simulation.unSubscribeDataManagerFromEvent(dataManagerId, eventClass);
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
	
	public void subscribeToSimulationClose(Consumer<ReportContext> consumer) {
		simulation.subscribeActorToSimulationClose(consumer);
	}
	
	
	public <T extends DataView> T getDataView(Class<T> dataManagerClass) {
		return simulation.getDataManagerForActor(dataManagerClass);
	}
	
	
	public double getTime() {
		return simulation.time;
	}
	
	
	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}
	
	
}
