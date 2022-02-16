package nucleus.testsupport.testplugin;

import java.util.Optional;
import java.util.function.Consumer;

import nucleus.DataManagerContext;

/**
 * Test Support class that describes an action for a data manager as a scheduled
 * plan with an optional key.
 */
public class TestDataManagerPlan {

	/*
	 * Key value generator for plans
	 */
	private static int masterKey;

	private static synchronized int getNextKey() {
		return masterKey++;
	}

	private final double scheduledTime;

	private final Object key;

	private final boolean releaseKey;

	private boolean executed;

	private final Consumer<DataManagerContext> action;

	public TestDataManagerPlan(TestDataManagerPlan testDataManagerPlan) {
		scheduledTime = testDataManagerPlan.scheduledTime;
		key = testDataManagerPlan.key;
		releaseKey = testDataManagerPlan.releaseKey;
		executed = testDataManagerPlan.executed;
		action = testDataManagerPlan.action;
	}

	public TestDataManagerPlan(final double scheduledTime, Consumer<DataManagerContext> action, boolean assignKey) {
		if (scheduledTime < 0) {
			throw new RuntimeException("negative scheduled time");
		}

		if (action == null) {
			throw new RuntimeException("null action plan");
		}
		this.scheduledTime = scheduledTime;
		this.key = getNextKey();
		this.releaseKey = assignKey;
		this.action = action;
	}

	public TestDataManagerPlan(final double scheduledTime, Consumer<DataManagerContext> action) {
		this(scheduledTime, action, true);
	}

	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan
	 * as executed.
	 */
	void executeAction(final DataManagerContext dataManagerContext) {
		try {
			action.accept(dataManagerContext);
		} finally {
			executed = true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (executed ? 1231 : 1237);
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (releaseKey ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(scheduledTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestDataManagerPlan)) {
			return false;
		}
		TestDataManagerPlan other = (TestDataManagerPlan) obj;
		if (executed != other.executed) {
			return false;
		}
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (releaseKey != other.releaseKey) {
			return false;
		}
		if (Double.doubleToLongBits(scheduledTime) != Double.doubleToLongBits(other.scheduledTime)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the key, possibly null, associated with this action plan
	 */
	public Optional<Object> getKey() {
		if (releaseKey) {
			return Optional.of(key);
		}
		return Optional.empty();
	}

	/**
	 * Returns the scheduled time for action execution
	 */
	public double getScheduledTime() {
		return scheduledTime;
	}

}
