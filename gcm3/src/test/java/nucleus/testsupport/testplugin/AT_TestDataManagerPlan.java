package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import util.RandomGeneratorProvider;

@UnitTest(target = TestDataManagerPlan.class)
public class AT_TestDataManagerPlan {

	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class })
	public void testConstructor() {

		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		});
		assertTrue(testDataManagerPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class, boolean.class })
	public void testConstructor_withKeyControl() {
		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, false);
		assertFalse(testDataManagerPlan.getKey().isPresent());

		testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, true);
		assertTrue(testDataManagerPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(args = { TestDataManagerPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7814286176804755234L);

		for (int i = 0; i < 100; i++) {
			double scheduledTime = randomGenerator.nextDouble();
			boolean useKey = randomGenerator.nextBoolean();
			TestDataManagerPlan originalTestDataManagerPlan = new TestDataManagerPlan(scheduledTime, (c) -> {
			}, useKey);
			TestDataManagerPlan newTestDataManagerPlan = new TestDataManagerPlan(originalTestDataManagerPlan);

			assertEquals(scheduledTime, newTestDataManagerPlan.getScheduledTime());
			assertEquals(useKey, newTestDataManagerPlan.getKey().isPresent());
			if (useKey) {
				assertEquals(originalTestDataManagerPlan.getKey().get(), newTestDataManagerPlan.getKey().get());
			}
		}
	}

	
	@Test
	@UnitTestMethod(name = "executed", args = {})
	public void testExecuted() {

		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, false);
		assertFalse(testDataManagerPlan.executed());
		testDataManagerPlan.executeAction(null);
		assertTrue(testDataManagerPlan.executed());

		TestDataManagerPlan testDataManagerPlanWithException = new TestDataManagerPlan(0.0, (c) -> {
			throw new RuntimeException();
		}, false);
		assertFalse(testDataManagerPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> testDataManagerPlanWithException.executeAction(null));
		assertTrue(testDataManagerPlanWithException.executed());
	}

	/**
	 * Show that action plans have or do not have keys as designed. Show that
	 * the keys are unique.
	 */
	@Test
	@UnitTestMethod(name = "getKey", args = {})
	public void testGetKey() {

		/*
		 * Show that an agent action plan created to not have a key in fact does
		 * not have one
		 */
		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, false);
		assertFalse(testDataManagerPlan.getKey().isPresent());

		/*
		 * Create a container to record the keys for the agent actions plans
		 * that will help us show that each key is unique
		 */
		Set<Object> keys = new LinkedHashSet<>();

		// use the constructor with explicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
			}, true);
			assertTrue(testDataManagerPlan.getKey().isPresent());
			boolean unique = keys.add(testDataManagerPlan.getKey().get());
			assertTrue(unique);
		}

		// use the constructor with implicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
			});
			assertTrue(testDataManagerPlan.getKey().isPresent());
			boolean unique = keys.add(testDataManagerPlan.getKey().get());
			assertTrue(unique);
		}

	}

	@Test
	@UnitTestMethod(name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9009925072863118451L);

		// use the various constructors
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			}, true);
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());

			testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			}, false);
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());

			testDataManagerPlan = new TestDataManagerPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testDataManagerPlan.getScheduledTime());
		}

	}
	

}
