package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportActionPlan.class)
public class AT_ReportActionPlan {
	
	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class })
	public void testConstructor() {
		//nothing to test		
	}
	/**
	 * Show that the report action plan can be executed and will result in
	 * executed() returning true even if an exception is thrown in the plan.
	 */
	@Test
	@UnitTestMethod(name = "executed", args = {})
	public void testExecuted() {
		ReportActionPlan reportActionPlan = new ReportActionPlan(0.0, (c) -> {
		});
		assertFalse(reportActionPlan.executed());
		reportActionPlan.executeAction(null);
		assertTrue(reportActionPlan.executed());
	}

	/**
	 * Shows that the scheduled time is returned correctly
	 */
	@Test
	@UnitTestMethod(name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2491009731104671942L);
		
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			ReportActionPlan reportActionPlan = new ReportActionPlan(planTime, (c) -> {
			});
			assertEquals(planTime, reportActionPlan.getScheduledTime());
		}
	}
}
