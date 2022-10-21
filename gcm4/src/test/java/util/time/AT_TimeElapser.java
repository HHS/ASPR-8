package util.time;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = TimeElapser.class)
public class AT_TimeElapser {

	@Test
	@UnitTestMethod(name = "getElapsedMilliSeconds", args = {})
	public void testGetElapsedMilliSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(name = "getElapsedNanoSeconds", args = {})
	public void testGetElapsedNanoSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(name = "getElapsedSeconds", args = {})
	public void testGetElapsedSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(name = "reset", args = {})
	public void testReset() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// TimeElapser is tested by manual inspection.
	}

}
