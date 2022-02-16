package nucleus.testsupport.testplugin;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class TestScenarioReport {
	private final boolean complete;

	public TestScenarioReport(boolean complete) {
		super();
		this.complete = complete;
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (complete ? 1231 : 1237);
		return result;
	}

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
