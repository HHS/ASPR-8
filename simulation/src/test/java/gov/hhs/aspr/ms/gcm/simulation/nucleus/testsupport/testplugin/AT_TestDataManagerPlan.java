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
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980855718377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			TestDataManagerPlan testDataManagerPlan = getRandomTestDataManagerPlan(randomGenerator.nextLong());
			assertFalse(testDataManagerPlan.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			TestDataManagerPlan testDataManagerPlan = getRandomTestDataManagerPlan(randomGenerator.nextLong());
			assertFalse(testDataManagerPlan.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			TestDataManagerPlan testDataManagerPlan = getRandomTestDataManagerPlan(randomGenerator.nextLong());
			assertTrue(testDataManagerPlan.equals(testDataManagerPlan));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestDataManagerPlan testDataManagerPlan1 = getRandomTestDataManagerPlan(seed);
			TestDataManagerPlan testDataManagerPlan2 = getRandomTestDataManagerPlan(seed);
			assertFalse(testDataManagerPlan1 == testDataManagerPlan2);
			for (int j = 0; j < 10; j++) {
				assertTrue(testDataManagerPlan1.equals(testDataManagerPlan2));
				assertTrue(testDataManagerPlan2.equals(testDataManagerPlan1));
			}

			// execute both plans and show they are still equal
			testDataManagerPlan1.executeAction(null);
			testDataManagerPlan2.executeAction(null);
			for (int j = 0; j < 10; j++) {
				assertTrue(testDataManagerPlan1.equals(testDataManagerPlan2));
				assertTrue(testDataManagerPlan2.equals(testDataManagerPlan1));
			}
		}

		// different inputs yield unequal testDataManagerPlans
		Set<TestDataManagerPlan> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestDataManagerPlan testDataManagerPlan = getRandomTestDataManagerPlan(randomGenerator.nextLong());
			set.add(testDataManagerPlan);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653444533465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestDataManagerPlan testDataManagerPlan1 = getRandomTestDataManagerPlan(seed);
			TestDataManagerPlan testDataManagerPlan2 = getRandomTestDataManagerPlan(seed);

			assertEquals(testDataManagerPlan1, testDataManagerPlan2);
			assertEquals(testDataManagerPlan1.hashCode(), testDataManagerPlan2.hashCode());

			// execute both plans and show they are still equal with equal hash codes
			testDataManagerPlan1.executeAction(null);
			testDataManagerPlan2.executeAction(null);
			assertEquals(testDataManagerPlan1, testDataManagerPlan2);
			assertEquals(testDataManagerPlan1.hashCode(), testDataManagerPlan2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestDataManagerPlan testDataManagerPlan = getRandomTestDataManagerPlan(randomGenerator.nextLong());
			hashCodes.add(testDataManagerPlan.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private TestDataManagerPlan getRandomTestDataManagerPlan(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return new TestDataManagerPlan(randomGenerator.nextDouble(), (c) -> {});
	}

}
