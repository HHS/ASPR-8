package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.Objects;
import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Test Support class that describes an action for a report as a scheduled plan.
 */
public class TestReportPlan {

	private final double scheduledTime;

	private boolean executed;

	private final Consumer<ReportContext> plan;

	/**
	 * Constructs an report action plan.
	 * 
	 * @throws ContractException {@linkplain TestError#NULL_PLAN} if the plan is
	 *                           null
	 */
	public TestReportPlan(final double scheduledTime, Consumer<ReportContext> plan) {

		if (plan == null) {
			throw new ContractException(TestError.NULL_PLAN);
		}

		this.scheduledTime = scheduledTime;

		this.plan = plan;
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(scheduledTime, executed);
	}

	/**
	 * Two {@link TestReportPlan} instances are equal if and only if
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
		TestReportPlan other = (TestReportPlan) obj;
		return Double.doubleToLongBits(scheduledTime) == Double.doubleToLongBits(other.scheduledTime)
				&& executed == other.executed;
	}

	/**
	 * Constructs an test report plan from another test report plan.
	 */
	public TestReportPlan(TestReportPlan testReportPlan) {
		scheduledTime = testReportPlan.scheduledTime;
		executed = testReportPlan.executed;
		plan = testReportPlan.plan;
	}

	/**
	 * Returns true if an only if this report action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan as
	 * executed.
	 */
	void executeAction(final ReportContext reportContext) {
		try {
			plan.accept(reportContext);
		} finally {
			executed = true;
		}
	}

	/**
	 * Returns the scheduled time for action execution
	 */
	public double getScheduledTime() {
		return scheduledTime;
	}

}
