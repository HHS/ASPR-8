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

public class AT_TestActorPlan {

	@Test
	@UnitTestConstructor(target = TestActorPlan.class, args = { double.class, Consumer.class })
	public void testConstructor() {

		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		});
		assertEquals(0.0, testActorPlan.getTime());
		assertFalse(testActorPlan.executed());
	}

	@Test
	@UnitTestConstructor(target = TestActorPlan.class, args = { TestActorPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7814286176804755234L);

		for (int i = 0; i < 100; i++) {
			double scheduledTime = randomGenerator.nextDouble();

			TestActorPlan originalTestActorPlan = new TestActorPlan(scheduledTime, (c) -> {
			});
			TestActorPlan newTestActorPlan = new TestActorPlan(originalTestActorPlan);

			assertEquals(scheduledTime, newTestActorPlan.getTime());

		}
	}

	/**
	 * Show that the agent action plan can be executed and will result in
	 * executed() returning true even if an exception is thrown in the plan.
	 */
	@Test
	@UnitTestMethod(target = TestActorPlan.class, name = "executed", args = {})
	public void testExecuted() {

		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		});
		assertFalse(testActorPlan.executed());
		testActorPlan.execute(null);
		assertTrue(testActorPlan.executed());

		TestActorPlan testActorPlanWithException = new TestActorPlan(0.0, (c) -> {
			throw new RuntimeException();
		});
		assertFalse(testActorPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> testActorPlanWithException.execute(null));
		assertTrue(testActorPlanWithException.executed());
	}

	@Test
	@UnitTestMethod(target = TestActorPlan.class, name = "equals", args = { Object.class })
	public void testEquals() {

		TestActorPlan plan1 = new TestActorPlan(4.5, (c) -> {
		});
		TestActorPlan plan2 = new TestActorPlan(4.5, (c) -> {
		});
		assertEquals(plan1, plan2);

		plan1 = new TestActorPlan(6.5, (c) -> {
		});
		plan2 = new TestActorPlan(4.5, (c) -> {
		});
		assertNotEquals(plan1, plan2);

	}

	@Test
	@UnitTestMethod(target = TestActorPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * show that equal objects have equal hash codes
		 */
		TestActorPlan plan1 = new TestActorPlan(4.5, (c) -> {
		});
		TestActorPlan plan2 = new TestActorPlan(4.5, (c) -> {
		});
		assertEquals(plan1.hashCode(), plan2.hashCode());

		// via the copy constructor
		plan1 = new TestActorPlan(4.5, (c) -> {
		});
		plan2 = new TestActorPlan(plan1);
		assertEquals(plan1.hashCode(), plan2.hashCode());

	}

}
