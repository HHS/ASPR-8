package nucleus.testsupport.actionplugin;

import java.util.Optional;
import java.util.function.Consumer;

import nucleus.DataManagerContext;

/**
 * Test Support class that describes an action for a data manager as a scheduled plan with an
 * optional key.
 */
public class DataManagerActionPlan {

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

	private final Consumer<DataManagerContext> action;

	public DataManagerActionPlan(DataManagerActionPlan dataManagerActionPlan) {
		scheduledTime = dataManagerActionPlan.scheduledTime;
		key = dataManagerActionPlan.key;
		executed = dataManagerActionPlan.executed;
		action = dataManagerActionPlan.action;
	}
	
	public DataManagerActionPlan(final double scheduledTime, Consumer<DataManagerContext> action, boolean assignKey) {
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

	public DataManagerActionPlan(final double scheduledTime, Consumer<DataManagerContext> action) {
		this(scheduledTime, action, true);
	}

	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan
	 * as executed.
	 */
	void executeAction(final DataManagerContext agentContext) {
		try {
			action.accept(agentContext);
		} finally {
			executed = true;
		}
	}

	/**
	 * Returns the key, possibly null, associated with this action plan
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
