package nucleus.testsupport.actionplugin;

import java.util.Optional;
import java.util.function.Consumer;

import nucleus.AgentContext;

/**
 * Test Support class that describes an action for an agent as a scheduled plan
 * with an optional key.
 */
public class AgentActionPlan {

	/*
	 * Key value generator for plans
	 */
	private static int masterKey;

	private static synchronized int getNextKey() {
		return masterKey++;
	}

	private final double scheduledTime;

	private final Object key;

	private boolean executed;

	private final Consumer<AgentContext> action;

	/**
	 * Constructs an agent action plan. If assignKey is false, then this agent
	 * action plan will return an empty optional key.
	 */
	public AgentActionPlan(final double scheduledTime, Consumer<AgentContext> action, boolean assignKey) {
		if (scheduledTime < 0) {
			throw new RuntimeException("negative scheduled time");
		}

		if (action == null) {
			throw new RuntimeException("null action plan");
		}
		this.scheduledTime = scheduledTime;
		if (assignKey) {
			this.key = getNextKey();
		} else {
			this.key = null;
		}
		this.action = action;
	}

	/**
	 * Constructs an agent action plan. A key value will be generated.
	 */
	public AgentActionPlan(final double scheduledTime, Consumer<AgentContext> action) {
		this(scheduledTime, action, true);
	}
	
	public AgentActionPlan(AgentActionPlan agentActionPlan) {
		scheduledTime = agentActionPlan.scheduledTime;
		key = agentActionPlan.key;
		executed = agentActionPlan.executed;
		action = agentActionPlan.action;
	}

	/**
	 * Returns true if an only if this agent action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan
	 * as executed.
	 */
	void executeAction(final AgentContext agentContext) {
		try {
			action.accept(agentContext);
		} finally {
			executed = true;
		}
	}

	/**
	 * Returns the key, possibly empty, associated with this action plan
	 */
	public Optional<Object> getKey() {
		return Optional.ofNullable(key);
	}

	/**
	 * Returns the scheduled time for action execution
	 */
	public double getScheduledTime() {
		return scheduledTime;
	}

	

}
