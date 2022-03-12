package nucleus.testsupport.testplugin;

import java.util.Optional;
import java.util.function.Consumer;

import nucleus.DataManagerContext;
import nucleus.util.ContractException;

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

	private final Consumer<DataManagerContext> plan;

	/**
	 * Constructs an test actor plan from another test actor plan.
	 */
	public TestDataManagerPlan(TestDataManagerPlan testDataManagerPlan) {
		scheduledTime = testDataManagerPlan.scheduledTime;
		key = testDataManagerPlan.key;
		releaseKey = testDataManagerPlan.releaseKey;
		executed = testDataManagerPlan.executed;
		plan = testDataManagerPlan.plan;
	}

	/**
	 * Constructs an data manager action plan. If assignKey is false, then this
	 * actor action plan will return an empty optional key.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain TestError#NEGATIVE_PLANNING_TIME} if the
	 *             scheduled plan time is negative</li>
	 *             <li>{@linkplain TestError#NULL_PLAN} if the plan is null</li>
	 */
	public TestDataManagerPlan(final double scheduledTime, Consumer<DataManagerContext> plan, boolean assignKey) {
		if (scheduledTime < 0) {
			throw new ContractException(TestError.NEGATIVE_PLANNING_TIME);
		}

		if (plan == null) {
			throw new ContractException(TestError.NULL_PLAN);
		}
		this.scheduledTime = scheduledTime;
		this.key = getNextKey();
		this.releaseKey = assignKey;
		this.plan = plan;
	}

	/**
	 * Constructs an data manager action plan. A key value will be generated.
	 */
	public TestDataManagerPlan(final double scheduledTime, Consumer<DataManagerContext> action) {
		this(scheduledTime, action, true);
	}

	/**
	 * Returns true if an only if this data manager action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan
	 * as executed.
	 */
	void executeAction(final DataManagerContext dataManagerContext) {
		try {
			plan.accept(dataManagerContext);
		} finally {
			executed = true;
		}
	}

	/**
	 * Boilerplate implementation of hashCode consistent with equals()
	 */
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

	/**
	 * Boilerplate implementation of equals. TestDataMangerPlans are equal if
	 * and only if all fields are equal.
	 */
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
