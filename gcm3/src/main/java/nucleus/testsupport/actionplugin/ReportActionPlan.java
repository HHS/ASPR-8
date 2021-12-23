package nucleus.testsupport.actionplugin;

import java.util.function.Consumer;

import nucleus.ReportContext;

/**
 * Test Support class that describes an action for a report as a scheduled plan.
 */
public class ReportActionPlan {

	private final double scheduledTime;

	private boolean executed;

	private final Consumer<ReportContext> action;

	public ReportActionPlan(final double scheduledTime, Consumer<ReportContext> action) {
		if (scheduledTime < 0) {
			throw new RuntimeException("negative scheduled time");
		}

		if (action == null) {
			throw new RuntimeException("null action plan");
		}
		this.scheduledTime = scheduledTime;
		this.action = action;
	}
	/**
	 * Return true if and only if the embedded action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	/**
	 * Package access. Executes the embedded action and marks this action plan
	 * as executed.
	 */
	void executeAction(final ReportContext reportContext) {
		try {
			action.accept(reportContext);
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
