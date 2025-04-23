package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.Objects;

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
	 * Returns true if and only if the plugin contained at least one plan for a test
	 * actor or test data manager and all such plans were executed by the
	 * simulation.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(complete);
	}

	/**
	 * Two {@link TestScenarioReport} instances are equal if and only if
	 * their inputs are equal.
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
		TestScenarioReport other = (TestScenarioReport) obj;
		return complete == other.complete;
	}

}
