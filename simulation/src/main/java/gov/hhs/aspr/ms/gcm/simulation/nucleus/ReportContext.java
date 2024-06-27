package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

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
	 * Schedules a passive report plan with a default arrival id that will be
	 * executed at the given time. Passive plans are not required to execute and the
	 * simulation will terminate if only passive plans remain on the planning
	 * schedule.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *                           null</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is added to the simulation after event
	 *                           processing is finished</li>
	 *                           </ul>
	 */
	public void addPlan(final Consumer<ReportContext> consumer, final double planTime) {
		simulation.addReportPlan(new ReportPlan(planTime, consumer));
	}

	/**
	 * Schedules a report plan. Plans arrival ids are ignored after the first wave
	 * of agent, report and data manager initialization during the simulation
	 * bootstrap. During the initialization phase, all plans with non-negative
	 * arrival ids (plans that were serialized) keep their arrival ids and all new
	 * plans (having arrival id = -1) are scheduled in the planning queue with
	 * higher arrival ids than all the serialized plans.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#NULL_PLAN} if the plan is
	 *                           null</li>
	 *                           <li>{@link NucleusError#PAST_PLANNING_TIME} if the
	 *                           plan is scheduled for a time in the past *</li>
	 *                           <li>{@link NucleusError#PLANNING_QUEUE_CLOSED} if
	 *                           the plan is added to the simulation after event
	 *                           processing is finished</li>
	 *                           <li>{@link NucleusError#INVALID_PLAN_ARRIVAL_ID} if
	 *                           the arrival id is less than -1</li>
	 *                           </ul>
	 */
	public void addPlan(ReportPlan plan) {
		simulation.addReportPlan(plan);
	}

	/**
	 * Returns true if and only if the reports should output their state as a plugin
	 * data instances at the end of the simulation.
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
	 * Subscribes the report to events of the given type for the purpose of
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
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ReportContext, T> eventConsumer) {
		simulation.subscribeReportToEvent(eventClass, eventConsumer);
	}

	/**
	 * Unsubscribes the report from events of the given type for all phases of event
	 * handling.
	 * 
	 * @throws ContractException {@link NucleusError#NULL_EVENT_CLASS} if the event
	 *                           class is null
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
	 *                           <li>{@linkplain NucleusError#NULL_DATA_MANAGER_CLASS}
	 *                           if the class reference is null</li>
	 *                           <li>{@linkplain NucleusError#UNKNOWN_DATA_MANAGER}
	 *                           if the class reference does not correspond to a
	 *                           contained datamanager</li>
	 *                           <li>
	 *                           {@linkplain NucleusError#AMBIGUOUS_DATA_MANAGER_CLASS}
	 *                           if the class reference points to more than 1
	 *                           datamanager</li>
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
	 * Returns the list of queued plans belonging to the current report. Should only
	 * be used after notification of simulation close.
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_ACTIVE}
	 *                           if this method is invoked before the termination of
	 *                           the simulation</li>
	 *                           </ul>
	 */
	public List<ReportPlan> retrievePlans() {
		return simulation.retrievePlansForReport();
	}

}
