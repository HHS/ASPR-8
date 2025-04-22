package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.Objects;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Test Support class that describes an action for a data manager as a scheduled plan.
 */
public class TestDataManagerPlan {

	private final double scheduledTime;

	private boolean executed;

	private final Consumer<DataManagerContext> plan;

	/**
	 * Constructs an test data manager plan from another test data manager plan.
	 */
	public TestDataManagerPlan(TestDataManagerPlan testDataManagerPlan) {
		scheduledTime = testDataManagerPlan.scheduledTime;
		executed = testDataManagerPlan.executed;
		plan = testDataManagerPlan.plan;
	}

	/**
	 * Constructs an data manager action plan.
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
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(scheduledTime, executed);
	}

	/**
	 * Two {@link TestDataManagerPlan} instances are equal if and only if
	 * they return the same values for 1) executed() and 2) getScheduledTime(). 
	 * This limited sense of equality is present simply to provide some reasonable 
	 * evidence that the plugin data cloning method is working correctly for the 
	 * test plugin data.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestDataManagerPlan other = (TestDataManagerPlan) obj;
		return Double.doubleToLongBits(scheduledTime) == Double.doubleToLongBits(other.scheduledTime)
				&& executed == other.executed;
	}

	/**
	 * Returns the scheduled time for action execution
	 */
	public double getScheduledTime() {
		return scheduledTime;
	}

}
