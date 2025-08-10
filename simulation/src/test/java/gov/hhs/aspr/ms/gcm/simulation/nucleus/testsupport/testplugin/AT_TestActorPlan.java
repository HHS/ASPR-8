package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
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
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8954621418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			TestActorPlan testActorPlan = getRandomTestActorPlan(randomGenerator.nextLong());
			assertFalse(testActorPlan.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			TestActorPlan testActorPlan = getRandomTestActorPlan(randomGenerator.nextLong());
			assertFalse(testActorPlan.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			TestActorPlan testActorPlan = getRandomTestActorPlan(randomGenerator.nextLong());
			assertTrue(testActorPlan.equals(testActorPlan));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestActorPlan testActorPlan1 = getRandomTestActorPlan(seed);
			TestActorPlan testActorPlan2 = getRandomTestActorPlan(seed);
			assertFalse(testActorPlan1 == testActorPlan2);
			for (int j = 0; j < 10; j++) {
				assertTrue(testActorPlan1.equals(testActorPlan2));
				assertTrue(testActorPlan2.equals(testActorPlan1));
			}

			// execute both plans and show they are still equal
			testActorPlan1.execute(null);
			testActorPlan2.execute(null);
			for (int j = 0; j < 10; j++) {
				assertTrue(testActorPlan1.equals(testActorPlan2));
				assertTrue(testActorPlan2.equals(testActorPlan1));
			}
		}

		// different inputs yield unequal testActorPlans
		Set<TestActorPlan> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestActorPlan testActorPlan = getRandomTestActorPlan(randomGenerator.nextLong());
			set.add(testActorPlan);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = TestActorPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653090533465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestActorPlan testActorPlan1 = getRandomTestActorPlan(seed);
			TestActorPlan testActorPlan2 = getRandomTestActorPlan(seed);

			assertEquals(testActorPlan1, testActorPlan2);
			assertEquals(testActorPlan1.hashCode(), testActorPlan2.hashCode());

			// execute both plans and show they are still equal with equal hash codes
			testActorPlan1.execute(null);
			testActorPlan2.execute(null);
			assertEquals(testActorPlan1, testActorPlan2);
			assertEquals(testActorPlan1.hashCode(), testActorPlan2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestActorPlan testActorPlan = getRandomTestActorPlan(randomGenerator.nextLong());
			hashCodes.add(testActorPlan.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private TestActorPlan getRandomTestActorPlan(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new TestActorPlan(randomGenerator.nextDouble(), (c) -> {});
	}
}
