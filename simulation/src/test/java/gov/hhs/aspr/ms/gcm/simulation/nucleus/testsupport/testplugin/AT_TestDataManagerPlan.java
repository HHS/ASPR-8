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

public class AT_TestDataManagerPlan {

	@Test
	@UnitTestConstructor(target = TestDataManagerPlan.class, args = { double.class, Consumer.class })
	public void testConstructor() {

		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(2.3, (c) -> {
		});
		assertEquals(2.3, testDataManagerPlan.getScheduledTime());
		assertFalse(testDataManagerPlan.executed());

	}

	@Test
	@UnitTestConstructor(target = TestDataManagerPlan.class, args = { TestDataManagerPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(340643142108466727L);

		for (int i = 0; i < 10; i++) {
			double scheduledTime = randomGenerator.nextDouble();

			TestDataManagerPlan originalTestDataManagerPlan = new TestDataManagerPlan(scheduledTime, (c) -> {
			});
			TestDataManagerPlan newTestDataManagerPlan = new TestDataManagerPlan(originalTestDataManagerPlan);

			assertEquals(scheduledTime, newTestDataManagerPlan.getScheduledTime());

		}
	}

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "executed", args = {})
	public void testExecuted() {

		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		});
		assertFalse(testDataManagerPlan.executed());
		testDataManagerPlan.executeAction(null);
		assertTrue(testDataManagerPlan.executed());

		TestDataManagerPlan testDataManagerPlanWithException = new TestDataManagerPlan(0.0, (c) -> {
			throw new RuntimeException();
		});
		assertFalse(testDataManagerPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> testDataManagerPlanWithException.executeAction(null));
		assertTrue(testDataManagerPlanWithException.executed());
	}

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9009925072863118451L);

		// use the various constructors
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());

			testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());

			testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());
		}

	}

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "equals", args = { Object.class })
	public void testEquals() {

		/*
		 * we must set the key release to false so that the auto generated key
		 * values won't cause the two plans to be unique
		 */
		TestDataManagerPlan plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		TestDataManagerPlan plan2 = new TestDataManagerPlan(4.5, (c) -> {
		});
		assertEquals(plan1, plan2);

		// with auto generated keys, there is no way to force them to be equal
		plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		plan2 = new TestDataManagerPlan(7.5, (c) -> {
		});
		assertNotEquals(plan1, plan2);

		// unless we use the copy constructor
		plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		plan2 = new TestDataManagerPlan(plan1);
		assertEquals(plan1, plan2);

	}

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * show that equal objects have equal hash codes
		 */
		TestDataManagerPlan plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		TestDataManagerPlan plan2 = new TestDataManagerPlan(4.5, (c) -> {
		});
		assertEquals(plan1.hashCode(), plan2.hashCode());

		// via the copy constructor
		plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		plan2 = new TestDataManagerPlan(plan1);
		assertEquals(plan1.hashCode(), plan2.hashCode());

	}

}
