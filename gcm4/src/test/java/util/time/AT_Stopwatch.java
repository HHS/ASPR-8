package util.time;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = Stopwatch.class)
public class AT_Stopwatch {
	
	@Test
	@UnitTestConstructor(args = {}, tags = { UnitTag.MANUAL })
	public void testConstructor() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "getElapsedMilliSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedMilliSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "getElapsedNanoSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedNanoSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "getElapsedSeconds", args = {}, tags = { UnitTag.MANUAL })
	public void testGetElapsedSeconds() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "getExecutionCount", args = {}, tags = { UnitTag.MANUAL })
	public void testGetExecutionCount() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "isRunning", args = {}, tags = { UnitTag.MANUAL })
	public void testIsRunning() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "reset", args = {}, tags = { UnitTag.MANUAL })
	public void testReset() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "start", args = {}, tags = { UnitTag.MANUAL })
	public void testStart() {
		// requires a manual test
	}

	@Test
	@UnitTestMethod(name = "stop", args = {}, tags = { UnitTag.MANUAL })
	public void testStop() {
		// requires a manual test
	}

}
