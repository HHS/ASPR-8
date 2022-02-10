package nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import util.ContractException;

/**
 * A resolver context provides access to the nucleus engine and published data
 * views to resolver. It is supplied by the engine each time it interacts with a
 * resolver. Resolvers are defined by this context. If this context is passed to
 * a method invocation, then that method is a resolver method.
 * 
 * @author Shawn Hatch
 *
 */

public interface DataManagerContext extends SimulationContext {

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
	 */
	public void addPlan(Consumer<DataManagerContext> plan, double planTime);

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
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 */
	public void addKeyedPlan(Consumer<DataManagerContext> plan, double planTime, Object key);

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution.Passive plans are not required to execute and the simulation
	 * will terminate if only passive plans remain on the planning schedule.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 * 
	 * 
	 */
	public void addPassivePlan(Consumer<DataManagerContext> plan, double planTime);

	/**
	 * Schedules a plan that will be executed at the given time. If the plan
	 * time is less than the current time the plan is scheduled for immediate
	 * execution. The plan is associated with the given key and can be canceled
	 * or retrieved via this key. Keys must be unique to the agent doing the
	 * planning, but can be repeated across agents and other planning entities.
	 * Use of keys with plans should be avoided unless retrieval or cancellation
	 * is needed.Passive plans are not required to execute and the simulation
	 * will terminate if only passive plans remain on the planning schedule.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key is
	 *             already in use by an existing plan
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan is
	 *             scheduled for a time in the past
	 */
	public void addKeyedPassivePlan(Consumer<DataManagerContext> plan, double planTime, Object key);

	/**
	 * Retrieves a plan for the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T extends Consumer<DataManagerContext>> T getPlan(final Object key);

	/**
	 * Returns the scheduled execution time for the plan associated with the
	 * given key
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public double getPlanTime(final Object key);

	/**
	 * Removes and returns the plan associated with the given key.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the plan key is
	 *             null
	 */
	public <T> Optional<T> removePlan(final Object key);

	/**
	 * Returns a list of the current plan keys associated with the current
	 * resolver
	 */
	public List<Object> getPlanKeys();

	/**
	 * Adds the given event to the event queue for eventual resolution and
	 * distribution.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT} if the event is null
	 */
	public void resolveEvent(Event event);

	/**
	 * Returns the AgentId of the current agent
	 */
	public Optional<AgentId> getCurrentAgentId();

	/**
	 * Returns true if and only if the given AgentId corresponds to an existing
	 * agent
	 */
	public boolean agentExists(AgentId agentId);

	/**
	 * Adds an agent to the simulation. The agent is added immediately, but the
	 * consumer of AgentContext is invoked after event resolution is finished
	 * and before time progresses.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_AGENT_ID} if the agentId is null
	 *             <li>{@link NucleusError#NULL_AGENT_CONTEXT_CONSUMER} if the
	 *             agent context consumer is null
	 *             <li>{@link NucleusError#NEGATIVE_AGENT_ID} if the agent id is
	 *             negative
	 *             <li>{@link NucleusError#AGENT_ID_IN_USE} if the agent id is
	 *             currently in use by another agent
	 * 
	 */
	public void addAgent(Consumer<AgentContext> init, AgentId agentId);

	/**
	 * Removes the given agent from the simulation.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_AGENT_ID} if the agentId is null
	 *             <li>{@link NucleusError#NEGATIVE_AGENT_ID} if the agent id is
	 *             negative
	 *             <li>{@link NucleusError#UNKNOWN_AGENT_ID} if the agent id
	 *             does not correspond to a known agent
	 */
	public void removeAgent(AgentId agentId);



	/**
	 * Terminates the simulation after the current plan is fully executed.
	 */
	public void halt();

	
	/**
	 * Subscribes the event resolver to events of the given type for the purpose
	 * of execution of the event. This occurs after the validation phase, so no
	 * validation of the event is required. Changes to data views should take
	 * place during this phase.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the resolver
	 *             event consumer is null
	 */
	public <T extends Event> void subscribeToEventExecutionPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer);

	/**
	 * Subscribes the event resolver to events of the given type for handling
	 * after the execution phase is complete. This should only be used for
	 * special cases where order of resolution is difficult to determine and the
	 * resolver needs to take action after all other resolvers. Dependency
	 * ordering in this phase is indeterminate and thus should only be used when
	 * the resolver will not generate observation events that might be consumed
	 * by other resolvers.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the resolver
	 *             event consumer is null
	 */
	public <T extends Event> void subscribeToEventPostPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer);

	/**
	 * Unsubscribes the event resolver from events of the given type for all
	 * phases of event handling.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 */
	public void unSubscribeToEvent(Class<? extends Event> eventClass);

	/**
	 * Adds an event labeler to nucleus.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_LABELER} if the event
	 *             labeler is null
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS_IN_EVENT_LABELER} if
	 *             the event class is null
	 *             <li>{@link NucleusError#NULL_LABELER_ID_IN_EVENT_LABELER} if
	 *             the event labeler contains a null labeler id
	 *             <li>{@link NucleusError#DUPLICATE_LABELER_ID_IN_EVENT_LABELER}
	 *             if the event labeler contains a labeler id that is the id of
	 *             a previously added event labeler
	 */
	public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler);

	

	/**
	 * Returns true if and only if there exists agent, report or resolver
	 * subscriptions to the given event class type.
	 */
	public boolean subscribersExistForEvent(Class<? extends Event> eventClass);

}
