package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_TestReportPlan {

	@Test
	@UnitTestConstructor(target = TestReportPlan.class, args = { double.class, Consumer.class })
	public void testConstructor() {

		TestReportPlan testReportPlan = new TestReportPlan(0.0, (c) -> {
		});
		assertEquals(0.0, testReportPlan.getScheduledTime());
		assertFalse(testReportPlan.executed());
	}

	@Test
	@UnitTestConstructor(target = TestReportPlan.class, args = { TestReportPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7814286176804755234L);

		for (int i = 0; i < 100; i++) {
			double scheduledTime = randomGenerator.nextDouble();

			TestReportPlan originalTestReportPlan = new TestReportPlan(scheduledTime, (c) -> {
			});
			TestReportPlan newTestReportPlan = new TestReportPlan(originalTestReportPlan);

			assertEquals(scheduledTime, newTestReportPlan.getScheduledTime());

		}
	}

	/**
	 * Show that the agent action plan can be executed and will result in
	 * executed() returning true even if an exception is thrown in the plan.
	 */
	@Test
	@UnitTestMethod(target = TestReportPlan.class, name = "executed", args = {})
	public void testExecuted() {

		TestReportPlan testReportPlan = new TestReportPlan(0.0, (c) -> {
		});
		assertFalse(testReportPlan.executed());
		testReportPlan.executeAction(null);
		assertTrue(testReportPlan.executed());

		TestReportPlan testReportPlanWithException = new TestReportPlan(0.0, (c) -> {
			throw new RuntimeException();
		});
		assertFalse(testReportPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> testReportPlanWithException.executeAction(null));
		assertTrue(testReportPlanWithException.executed());
	}

	

	@Test
	@UnitTestMethod(target = TestReportPlan.class, name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(918257164535899051L);

		// use the various constructors
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			TestReportPlan testReportPlan = new TestReportPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testReportPlan.getScheduledTime());

			testReportPlan = new TestReportPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testReportPlan.getScheduledTime());

			testReportPlan = new TestReportPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testReportPlan.getScheduledTime());
		}

	}

	@Test
	@UnitTestMethod(target = TestReportPlan.class, name = "equals", args = { Object.class })
	public void testEquals() {

		
		TestReportPlan plan1 = new TestReportPlan(4.5, (c) -> {
		});
		TestReportPlan plan2 = new TestReportPlan(4.5, (c) -> {
		});
		assertEquals(plan1, plan2);

		
		plan1 = new TestReportPlan(6.5, (c) -> {
		});
		plan2 = new TestReportPlan(4.5, (c) -> {
		});
		assertNotEquals(plan1, plan2);

		

	}

	@Test
	@UnitTestMethod(target = TestReportPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * show that equal objects have equal hash codes
		 */
		TestReportPlan plan1 = new TestReportPlan(4.5, (c) -> {
		});
		TestReportPlan plan2 = new TestReportPlan(4.5, (c) -> {
		});
		assertEquals(plan1.hashCode(), plan2.hashCode());

		// via the copy constructor
		plan1 = new TestReportPlan(4.5, (c) -> {
		});
		plan2 = new TestReportPlan(plan1);
		assertEquals(plan1.hashCode(), plan2.hashCode());

	}

}
