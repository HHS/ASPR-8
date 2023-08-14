package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import net.jcip.annotations.ThreadSafe;

/**
 * A thread safe report supporting analysis for scenario completion. A test
 * plugin scenario is considered complete if and only if the plugin contained at
 * least one plan for a test actor or test data manager and all such plans were
 * executed by the simulation.
 */
@ThreadSafe
public final class TestScenarioReport {
	private final boolean complete;

	public TestScenarioReport(boolean complete) {
		super();
		this.complete = complete;
	}

	/**
	 * Returns true if and only if the plugin contained at least one plan for a
	 * test actor or test data manager and all such plans were executed by the
	 * simulation.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Hash code implementation consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (complete ? 1231 : 1237);
		return result;
	}

	/**
	 * Two TestScenarioReport instances are equal if and only if they have the
	 * same completion status.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestScenarioReport)) {
			return false;
		}
		TestScenarioReport other = (TestScenarioReport) obj;
		if (complete != other.complete) {
			return false;
		}
		return true;
	}

}
