package nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import util.ContractException;

/**
 * An agent context provides access to the nucleus engine and published data
 * views to agents. It is supplied by the engine each time it interacts with an
 * agent. Agents are defined by this context. If this context is passed to a
 * method invocation, then that method is an agent method.
 * 
 * @author Shawn Hatch
 *
 */

public interface AgentContext extends SimulationContext {

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution.
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
	public void addPlan(Consumer<AgentContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution. The plan is associated with the given key and can be canceled
	 * or retrieved via this key. Keys must be unique to the agent doing the
	 * planning, but can be repeated across agents and other planning entities.
	 * Use of keys with plans should be avoided unless retrieval or cancellation
	 * is needed.
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
	public void addKeyedPlan(Consumer<AgentContext> plan, double planTime, Object key);

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution. Passive plans are not required to execute and the simulation
	 * will terminate if only passive plans remain on the planning schedule.
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
	public void addPassivePlan(Consumer<AgentContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution. The plan is associated with the given key and can be canceled
	 * or retrieved via this key. Keys must be unique to the agent doing the
	 * planning, but can be repeated across agents and other planning entities.
	 * Use of keys with plans should be avoided unless retrieval or cancellation
	 * is needed. Passive plans are not required to execute and the simulation
	 * will terminate if only passive plans remain on the planning schedule.
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
	public void addPassiveKeyedPlan(Consumer<AgentContext> plan, double planTime, Object key);

	/**
	 * Retrieves a plan for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T extends Consumer<AgentContext>> Optional<T> getPlan(final Object key);

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
	 * Returns a list of the current plan keys associated with the current agent
	 * 
	 */
	public List<Object> getPlanKeys();

	/**
	 * Returns the AgentId of the current agent
	 */
	public AgentId getAgentId();

	

	

	/**
	 * Subscribes the current agent to the given event label. Events of the type
	 * T that are generated by event resolution (not events generated by agents)
	 * are matched to the event label. If a match is found, then the event will
	 * be consumed by the supplied AgentEventConsumer.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_LABEL} if the EventLabel
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the
	 *             AgentEventConsumer is null
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS_IN_EVENT_LABEL} if
	 *             the event class in the event label is null
	 *             <li>{@link NucleusError#NULL_LABELER_ID_IN_EVENT_LABEL} if
	 *             the event labeler id in the event label is null
	 *             <li>{@link NucleusError#UNKNOWN_EVENT_LABELER} if the event
	 *             labeler id in the event label cannot be resolved to a
	 *             registered event labeler *
	 *             <li>{@link NucleusError#NULL_PRIMARY_KEY_VALUE} if the event
	 *             label has a null primary key
	 * 
	 */
	public <T extends Event> void subscribe(EventLabel<T> eventLabel, BiConsumer<AgentContext,T> eventConsumer);
	
	/**
	 * Subscribes the current agent to the given event. 
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the
	 *             ReportEventConsumer is null
	 * 
	 */
	public <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<AgentContext,T> eventConsumer);
	/**
	 * Unsubscribes the current agent from the given event label.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_LABEL} if the EventLabel
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS_IN_EVENT_LABELER} if
	 *             the event class in the event label is null
	 *             <li>{@link NucleusError#NULL_LABELER_ID_IN_EVENT_LABEL} if
	 *             the event labeler id in the event label is null
	 *             <li>{@link NucleusError#UNKNOWN_EVENT_LABELER} if the event
	 *             labeler id in the event label cannot be resolved to a
	 *             registered event labeler
	 *             <li>{@link NucleusError#NULL_PRIMARY_KEY_VALUE} if the event
	 *             label has a null primary key
	 */
	public <T extends Event> void unsubscribe(EventLabel<T> eventLabel);

	
	
	/**
	 * Subscribes the current report to have the given ReportContext consumer
	 * invoked at the end of the simulation.
	 */
	public void subscribeToSimulationClose(Consumer<AgentContext> closeHandler);

}
