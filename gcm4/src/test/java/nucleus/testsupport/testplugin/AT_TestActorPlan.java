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

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = TestActorPlan.class)
public class AT_TestActorPlan {

	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class })
	public void testConstructor() {

		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		});
		assertTrue(testActorPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class, boolean.class })
	public void testConstructor_withKeyControl() {
		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		}, false);
		assertFalse(testActorPlan.getKey().isPresent());

		testActorPlan = new TestActorPlan(0.0, (c) -> {
		}, true);
		assertTrue(testActorPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(args = { TestActorPlan.class })
	public void testConstructor_fromExistingPlan() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7814286176804755234L);

		for (int i = 0; i < 100; i++) {
			double scheduledTime = randomGenerator.nextDouble();
			boolean useKey = randomGenerator.nextBoolean();
			TestActorPlan originalTestActorPlan = new TestActorPlan(scheduledTime, (c) -> {
			}, useKey);
			TestActorPlan newTestActorPlan = new TestActorPlan(originalTestActorPlan);

			assertEquals(scheduledTime, newTestActorPlan.getScheduledTime());
			assertEquals(useKey, newTestActorPlan.getKey().isPresent());
			if (useKey) {
				assertEquals(originalTestActorPlan.getKey().get(), newTestActorPlan.getKey().get());
			}
		}
	}

	/**
	 * Show that the agent action plan can be executed and will result in
	 * executed() returning true even if an exception is thrown in the plan.
	 */
	@Test
	@UnitTestMethod(name = "executed", args = {})
	public void testExecuted() {

		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		}, false);
		assertFalse(testActorPlan.executed());
		testActorPlan.executeAction(null);
		assertTrue(testActorPlan.executed());

		TestActorPlan testActorPlanWithException = new TestActorPlan(0.0, (c) -> {
			throw new RuntimeException();
		}, false);
		assertFalse(testActorPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> testActorPlanWithException.executeAction(null));
		assertTrue(testActorPlanWithException.executed());
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
		TestActorPlan testActorPlan = new TestActorPlan(0.0, (c) -> {
		}, false);
		assertFalse(testActorPlan.getKey().isPresent());

		/*
		 * Create a container to record the keys for the agent actions plans
		 * that will help us show that each key is unique
		 */
		Set<Object> keys = new LinkedHashSet<>();

		// use the constructor with explicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			testActorPlan = new TestActorPlan(0.0, (c) -> {
			}, true);
			assertTrue(testActorPlan.getKey().isPresent());
			boolean unique = keys.add(testActorPlan.getKey().get());
			assertTrue(unique);
		}

		// use the constructor with implicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			testActorPlan = new TestActorPlan(0.0, (c) -> {
			});
			assertTrue(testActorPlan.getKey().isPresent());
			boolean unique = keys.add(testActorPlan.getKey().get());
			assertTrue(unique);
		}

	}

	@Test
	@UnitTestMethod(name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(918257164535899051L);

		// use the various constructors
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			TestActorPlan testActorPlan = new TestActorPlan(planTime, (c) -> {
			}, true);
			assertEquals(planTime, testActorPlan.getScheduledTime());

			testActorPlan = new TestActorPlan(planTime, (c) -> {
			}, false);
			assertEquals(planTime, testActorPlan.getScheduledTime());

			testActorPlan = new TestActorPlan(planTime, (c) -> {
			});
			assertEquals(planTime, testActorPlan.getScheduledTime());
		}

	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {

		/*
		 * we must set the key release to false so that the auto generated key
		 * values won't cause the two plans to be unique
		 */
		TestActorPlan plan1 = new TestActorPlan(4.5, (c) -> {
		}, false);
		TestActorPlan plan2 = new TestActorPlan(4.5, (c) -> {
		}, false);
		assertEquals(plan1, plan2);
		
		//with auto generated keys, there is no way to force them to be equal
		plan1 = new TestActorPlan(4.5, (c) -> {
		});
		plan2 = new TestActorPlan(4.5, (c) -> {
		});
		assertNotEquals(plan1, plan2);
		
		//unless we use the copy constructor
		plan1 = new TestActorPlan(4.5, (c) -> {
		});
		plan2 = new TestActorPlan(plan1);
		assertEquals(plan1, plan2);

	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		/*
		 * show that equal objects have equal hash codes
		 */
		TestActorPlan plan1 = new TestActorPlan(4.5, (c) -> {
		}, false);
		TestActorPlan plan2 = new TestActorPlan(4.5, (c) -> {
		}, false);
		assertEquals(plan1.hashCode(), plan2.hashCode());
		
		//via the copy constructor
		plan1 = new TestActorPlan(4.5, (c) -> {
		});
		plan2 = new TestActorPlan(plan1);
		assertEquals(plan1.hashCode(), plan2.hashCode());

	}

}
