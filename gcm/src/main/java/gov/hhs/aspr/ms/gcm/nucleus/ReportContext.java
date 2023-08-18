package gov.hhs.aspr.ms.gcm.nucleus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import util.errors.ContractException;

/**
 * An report context provides access to the nucleus simulation. It is supplied
 * by the simulation each time it interacts with an report. Reports are defined
 * by this context. If this context is passed to a method invocation, then that
 * method is a report method.
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
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is
	 *                           scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is
	 *                           added to the simulation after event processing is
	 *                           finished</li>
	 *                           </ul>
	 */
	public void addPlan(final Consumer<ReportContext> consumer, final double planTime) {
		Plan<ReportContext> plan = Plan.builder(ReportContext.class)//
				.setActive(false)//
				.setCallbackConsumer(consumer)//
				.setKey(null)//
				.setPlanData(null)//
				.setTime(planTime)//
				.build();//
		simulation.addReportPlan(plan);
	}

	/**
	 * Schedules a plan.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is
	 *                           scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is
	 *                           added to the simulation after event processing is
	 *                           finished</li>
	 *                           </ul>
	 */
	public void addPlan(Plan<ReportContext> plan) {
		if (plan == null) {
			throw new ContractException(NucleusError.NULL_PLAN);
		}
		simulation.addReportPlan(plan);
	}

	/**
	 * Retrieves a plan stored for the given key.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN_KEY} if the plan
	 *                           key is
	 *                           null</li>
	 *                           </ul>
	 */
	public Optional<Plan<ReportContext>> getPlan(final Object key) {
		return simulation.getReportPlan(key);
	}

	/**
	 * Returns true if and only if the reports should output their state as a
	 * plugin data instances at the end of the simulation.
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
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_PLAN_KEY} if the plan
	 *                           key is null
	 */
	public Optional<Plan<ReportContext>> removePlan(final Object key) {
		return simulation.removeReportPlan(key);
	}

	/**
	 * Returns a list of the current plan keys associated with the current
	 * report
	 */
	public List<Object> getPlanKeys() {
		return simulation.getReportPlanKeys();
	}

	/**
	 * Subscribes the report to events of the given type for the purpose of
	 * execution of data changes.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_EVENT_CLASS} if the
	 *                           event class
	 *                           is null</li>
	 *                           <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the
	 *                           event
	 *                           consumer is null</li>
	 *                           <li>{@link NucleusError#DUPLICATE_EVENT_SUBSCRIPTION}
	 *                           if the
	 *                           data manager is already subscribed</li>
	 *                           </ul>
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ReportContext, T> eventConsumer) {
		simulation.subscribeReportToEvent(eventClass, eventConsumer);
	}

	/**
	 * Unsubscribes the report from events of the given type for all phases of
	 * event handling.
	 * 
	 * @throws ContractExceptionn {@link NucleusError#NULL_EVENT_CLASS} if the
	 *                            event class is null
	 */
	public void unsubscribe(Class<? extends Event> eventClass) {
		simulation.unsubscribeReportFromEvent(eventClass);
	}

	/**
	 * Registers the given consumer to be executed at the end of the simulation.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_REPORT_CONTEXT_CONSUMER}
	 *                           if the consumer is null
	 */
	public void subscribeToSimulationClose(Consumer<ReportContext> consumer) {
		simulation.subscribeReportToSimulationClose(consumer);
	}

	/**
	 * Returns the DataManger instance from the given class reference
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_DATA_VIEW_CLASS}
	 *                           if the
	 *                           class reference is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_DATA_VIEW} if
	 *                           the class
	 *                           reference does not correspond to a contained data
	 *                           view</li>
	 *                           </ul>
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

	/**
	 * Sets a function for converting plan data instances into consumers of
	 * actor context that will be used to convert stored plans from a previous
	 * simulation execution into current plans. Only used during the
	 * initialization of the simulation before time flows.
	 */
	public <T extends PlanData> void setPlanDataConverter(Class<T> planDataClass,
			Function<T, Consumer<ReportContext>> conversionFunction) {
		simulation.setReportPlanDataConverter(planDataClass, conversionFunction);
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
