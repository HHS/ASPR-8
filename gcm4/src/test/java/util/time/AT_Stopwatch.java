package util.time;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_Stopwatch {

	@Test
	@UnitTestConstructor(target = Stopwatch.class, args = {}, tags = { UnitTag.MANUAL })
	public void testConstructor() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "getElapsedMilliSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedMilliSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "getElapsedNanoSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedNanoSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "getElapsedSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "getExecutionCount", args = {}, tags = { UnitTag.MANUAL })
	public void testGetExecutionCount() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "isRunning", args = {}, tags = { UnitTag.MANUAL })
	public void testIsRunning() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "reset", args = {}, tags = { UnitTag.MANUAL })
	public void testReset() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "start", args = {}, tags = { UnitTag.MANUAL })
	public void testStart() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(target = Stopwatch.class, name = "stop", args = {}, tags = { UnitTag.MANUAL })
	public void testStop() {
		// requires a manual test
	}

}
