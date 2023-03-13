package nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * An report context provides access to the nucleus simulation. It is supplied
 * by the simulation each time it interacts with an report. Reports are defined
 * by this context. If this context is passed to a method invocation, then that
 * method is a report method.
 * 
 *
 */

public final class ReportContext {
	
	public ReportId getReportId() {
		return simulation.focalReportId;
	}

	private final Simulation simulation;

	protected ReportContext(Simulation simulation) {
		this.simulation = simulation;
	}

	/**
	 * Schedules a passive plan that will be executed at the given time. Passive
	 * plans are not required to execute and the simulation will terminate if
	 * only passive plans remain on the planning schedule.
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
	public void addPlan(final Consumer<ReportContext> plan, final double planTime) {
		simulation.addReportPlan(plan, planTime, null);
	}

	/**
	 * Schedules a passive plan that will be executed at the given time. The
	 * plan is associated with the given key and can be canceled or retrieved
	 * via this key. Keys must be unique to the report doing the planning, but
	 * can be repeated across reports and other planning entities. Use of keys
	 * with plans should be avoided unless retrieval or cancellation is needed.
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

	public void addKeyedPlan(final Consumer<ReportContext> plan, final double planTime, final Object key) {
		simulation.validatePlanKeyNotNull(key);
		simulation.validateReportPlanKeyNotDuplicate(key);
		simulation.addReportPlan(plan, planTime, key);
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
		return (Optional<T>) simulation.getReportPlan(key);
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
		return simulation.getReportPlanTime(key);
	}

	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */

	public <T extends Consumer<ReportContext>> Optional<T> removePlan(final Object key) {
		return simulation.removeReportPlan(key);
	}

	/**
	 * Returns a list of the current plan keys associated with the current report
	 * 
	 */
	 public List<Object> getPlanKeys() {
	 return simulation.getReportPlanKeys();
	 }

	/**
	 * Subscribes the report to events of the given type for the purpose of
	 * execution of data changes.
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
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ReportContext, T> eventConsumer) {
		simulation.subscribeReportToEvent(eventClass, eventConsumer);
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
	public void subscribeToSimulationState(BiConsumer<ReportContext, SimulationStateContext> consumer) {
		simulation.subscribeReportToSimulationState(consumer);
	}

	/**
	 * Unsubscribes the report from events of the given type for all phases of
	 * event handling.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass) {
		simulation.unsubscribeReportFromEvent(eventClass);
	}

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REPORT_CONTEXT_CONSUMER} if the
	 *             consumer is null</li>
	 */
	public void subscribeToSimulationClose(Consumer<ReportContext> consumer) {
		simulation.subscribeReportToSimulationClose(consumer);
	}

	/**
	 * Returns the DataManger instance from the given class reference
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_DATA_VIEW_CLASS} if the
	 *             class reference is null</li>
	 *             <li>{@linkplain NucleusError#UNKNOWN_DATA_VIEW} if the class
	 *             reference does not correspond to a contained data view</li>
	 * 
	 */

	public <T extends DataManager> T getDataManager(Class<T> dataManagerClass) {
		return simulation.getDataManagerForActor(dataManagerClass);
	}

	public double getTime() {
		return simulation.time;
	}

	public void releaseOutput(Object output) {
		simulation.releaseOutput(output);
	}

}
