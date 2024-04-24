package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_StatusConsoleState {

	@Test
	@UnitTestConstructor(target = StatusConsoleState.class, args = {})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "immediateErrorReporting", args = {})
	public void testImmediateErrorReporting() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertFalse(statusConsoleState.immediateErrorReporting());

		statusConsoleState.setImmediateErrorReporting(true);
		assertTrue(statusConsoleState.immediateErrorReporting());

		statusConsoleState.setImmediateErrorReporting(false);
		assertFalse(statusConsoleState.immediateErrorReporting());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "setImmediateErrorReporting", args = { boolean.class })
	public void testSetImmediateErrorReporting() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertFalse(statusConsoleState.immediateErrorReporting());

		statusConsoleState.setImmediateErrorReporting(true);
		assertTrue(statusConsoleState.immediateErrorReporting());

		statusConsoleState.setImmediateErrorReporting(false);
		assertFalse(statusConsoleState.immediateErrorReporting());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "reportScenarioProgress", args = {})
	public void testReportScenarioProgress() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertFalse(statusConsoleState.reportScenarioProgress());

		statusConsoleState.setReportScenarioProgress(true);
		assertTrue(statusConsoleState.reportScenarioProgress());

		statusConsoleState.setReportScenarioProgress(false);
		assertFalse(statusConsoleState.reportScenarioProgress());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "setReportScenarioProgress", args = { boolean.class })
	public void testSetReportScenarioProgress() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();

		statusConsoleState.setReportScenarioProgress(true);
		assertTrue(statusConsoleState.reportScenarioProgress());

		statusConsoleState.setReportScenarioProgress(false);
		assertFalse(statusConsoleState.reportScenarioProgress());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "getStackTraceReportLimit", args = {})
	public void testGetStackTraceReportLimit() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertEquals(0, statusConsoleState.getStackTraceReportLimit());

		statusConsoleState.setStackTraceReportLimit(35);
		assertEquals(35, statusConsoleState.getStackTraceReportLimit());

		statusConsoleState.setStackTraceReportLimit(10);
		assertEquals(10, statusConsoleState.getStackTraceReportLimit());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "setStackTraceReportLimit", args = { int.class })
	public void testSetStackTraceReportLimit() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();

		statusConsoleState.setStackTraceReportLimit(35);
		assertEquals(35, statusConsoleState.getStackTraceReportLimit());

		statusConsoleState.setStackTraceReportLimit(10);
		assertEquals(10, statusConsoleState.getStackTraceReportLimit());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "getLastReportedCompletionPercentage", args = {})
	public void testGetLastReportedCompletionPercentage() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertEquals(0, statusConsoleState.getLastReportedCompletionPercentage());

		statusConsoleState.setLastReportedCompletionPercentage(14);
		assertEquals(14, statusConsoleState.getLastReportedCompletionPercentage());

	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "setLastReportedCompletionPercentage", args = { int.class })
	public void testSetLastReportedCompletionPercentage() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();

		statusConsoleState.setLastReportedCompletionPercentage(14);
		assertEquals(14, statusConsoleState.getLastReportedCompletionPercentage());

		statusConsoleState.setLastReportedCompletionPercentage(88);
		assertEquals(88, statusConsoleState.getLastReportedCompletionPercentage());

		statusConsoleState.setLastReportedCompletionPercentage(57);
		assertEquals(57, statusConsoleState.getLastReportedCompletionPercentage());
	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "getImmediateStackTraceCount", args = {})
	public void testGetImmediateStackTraceCount() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();
		assertEquals(0, statusConsoleState.getImmediateStackTraceCount());

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(1, statusConsoleState.getImmediateStackTraceCount());

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(2, statusConsoleState.getImmediateStackTraceCount());

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(3, statusConsoleState.getImmediateStackTraceCount());

	}

	@Test
	@UnitTestMethod(target = StatusConsoleState.class, name = "incrementImmediateStackTraceCount", args = {})
	public void testIncrementImmediateStackTraceCount() {
		StatusConsoleState statusConsoleState = new StatusConsoleState();

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(1, statusConsoleState.getImmediateStackTraceCount());

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(2, statusConsoleState.getImmediateStackTraceCount());

		statusConsoleState.incrementImmediateStackTraceCount();
		assertEquals(3, statusConsoleState.getImmediateStackTraceCount());
	}

}
