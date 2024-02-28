package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Test Support class that describes an action for a data manager as a scheduled
 * plan with an optional key.
 */
public class TestDataManagerPlan {

	private final double scheduledTime;

	private boolean executed;

	private final Consumer<DataManagerContext> plan;

	/**
	 * Constructs an test actor plan from another test actor plan.
	 */
	public TestDataManagerPlan(TestDataManagerPlan testDataManagerPlan) {
		scheduledTime = testDataManagerPlan.scheduledTime;
		executed = testDataManagerPlan.executed;
		plan = testDataManagerPlan.plan;
	}

	/**
	 * Constructs an data manager action plan. If assignKey is false, then this
	 * actor action plan will return an empty optional key.
	 * 
	 * @throws ContractException {@linkplain TestError#NULL_PLAN} if the plan is
	 *                           null
	 */
	public TestDataManagerPlan(final double scheduledTime, Consumer<DataManagerContext> plan) {

		if (plan == null) {
			throw new ContractException(TestError.NULL_PLAN);
		}
		this.scheduledTime = scheduledTime;
		this.plan = plan;
	}

	/**
	 * Returns true if an only if this data manager action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan as
	 * executed.
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
		long temp;
		temp = Double.doubleToLongBits(scheduledTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * TestDataManagerPlans are equal if and only they return the same values for
	 * 1)getKey(), 2)executed() and 3)getScheduledTime()This limited sense of
	 * equality is present simply to provide some reasonable evidence that the
	 * plugin data cloning method is working correctly for the test plugin data.
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

		if (Double.doubleToLongBits(scheduledTime) != Double.doubleToLongBits(other.scheduledTime)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the scheduled time for action execution
	 */
	public double getScheduledTime() {
		return scheduledTime;
	}

}
