package util.time;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_TimeElapser {

	@Test
	@UnitTestMethod(target = TimeElapser.class, name = "getElapsedMilliSeconds", args = {})
	public void testGetElapsedMilliSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(target = TimeElapser.class, name = "getElapsedNanoSeconds", args = {})
	public void testGetElapsedNanoSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(target = TimeElapser.class, name = "getElapsedSeconds", args = {})
	public void testGetElapsedSeconds() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestMethod(target = TimeElapser.class, name = "reset", args = {})
	public void testReset() {
		// TimeElapser is tested by manual inspection.
	}

	@Test
	@UnitTestConstructor(target = TimeElapser.class, args = {})
	public void testConstructor() {
		// TimeElapser is tested by manual inspection.
	}

}
