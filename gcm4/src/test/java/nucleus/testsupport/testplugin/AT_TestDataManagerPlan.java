package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestDataManagerPlan {

	@Test
	@UnitTestConstructor(target = TestDataManagerPlan.class, args = { double.class, Consumer.class })
	public void testConstructor() {

		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		});
		assertTrue(testDataManagerPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(target = TestDataManagerPlan.class, args = { double.class, Consumer.class, boolean.class })
	public void testConstructor_withKeyControl() {
		TestDataManagerPlan testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, false);
		assertFalse(testDataManagerPlan.getKey().isPresent());

		testDataManagerPlan = new TestDataManagerPlan(0.0, (c) -> {
		}, true);
		assertTrue(testDataManagerPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(target = TestDataManagerPlan.class, args = { TestDataManagerPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(340643142108466727L);

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
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "executed", args = {})
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
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "getKey", args = {})
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
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "getScheduledTime", args = {})
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

	@Test
	@UnitTestMethod(target = TestDataManagerPlan.class, name = "equals", args = { Object.class })
	public void testEquals() {

		/*
		 * we must set the key release to false so that the auto generated key
		 * values won't cause the two plans to be unique
		 */
		TestDataManagerPlan plan1 = new TestDataManagerPlan(4.5, (c) -> {
		}, false);
		TestDataManagerPlan plan2 = new TestDataManagerPlan(4.5, (c) -> {
		}, false);
		assertEquals(plan1, plan2);

		// with auto generated keys, there is no way to force them to be equal
		plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		plan2 = new TestDataManagerPlan(4.5, (c) -> {
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
		}, false);
		TestDataManagerPlan plan2 = new TestDataManagerPlan(4.5, (c) -> {
		}, false);
		assertEquals(plan1.hashCode(), plan2.hashCode());

		// via the copy constructor
		plan1 = new TestDataManagerPlan(4.5, (c) -> {
		});
		plan2 = new TestDataManagerPlan(plan1);
		assertEquals(plan1.hashCode(), plan2.hashCode());

	}

}
