package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
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
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8980821418377306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			TestReportPlan testReportPlan = getRandomTestReportPlan(randomGenerator.nextLong());
			assertFalse(testReportPlan.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			TestReportPlan testReportPlan = getRandomTestReportPlan(randomGenerator.nextLong());
			assertFalse(testReportPlan.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			TestReportPlan testReportPlan = getRandomTestReportPlan(randomGenerator.nextLong());
			assertTrue(testReportPlan.equals(testReportPlan));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestReportPlan testReportPlan1 = getRandomTestReportPlan(seed);
			TestReportPlan testReportPlan2 = getRandomTestReportPlan(seed);
			assertFalse(testReportPlan1 == testReportPlan2);
			for (int j = 0; j < 10; j++) {
				assertTrue(testReportPlan1.equals(testReportPlan2));
				assertTrue(testReportPlan2.equals(testReportPlan1));
			}
		}

		// different inputs yield unequal testReportPlans
		Set<TestReportPlan> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestReportPlan testReportPlan = getRandomTestReportPlan(randomGenerator.nextLong());
			set.add(testReportPlan);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = TestReportPlan.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2653491533465183354L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			TestReportPlan testReportPlan1 = getRandomTestReportPlan(seed);
			TestReportPlan testReportPlan2 = getRandomTestReportPlan(seed);

			assertEquals(testReportPlan1, testReportPlan2);
			assertEquals(testReportPlan1.hashCode(), testReportPlan2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			TestReportPlan testReportPlan = getRandomTestReportPlan(randomGenerator.nextLong());
			hashCodes.add(testReportPlan.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	private static List<Consumer<ReportContext>> staticConsumers = new ArrayList<>();

	/*
	 * Thread safe means of creating a list of 20 consumers. Comparing consumers for
	 * equality is implemented via == comparison in Java, so we need to create a
	 * static set of them. 
	 */
	private static List<Consumer<ReportContext>> getStaticConsumers() {
		synchronized (staticConsumers) {
			if (staticConsumers.isEmpty()) {
				staticConsumers = new ArrayList<>();
				for (int i = 0; i < 20; i++) {
					staticConsumers.add((c) -> {
					});
				}
			}
		}
		return staticConsumers;
	}

	private TestReportPlan getRandomTestReportPlan(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Consumer<ReportContext>> staticConsumers = getStaticConsumers();
		int randomIndex = randomGenerator.nextInt(staticConsumers.size());
		Consumer<ReportContext> randomConsumer = staticConsumers.get(randomIndex);

		return new TestReportPlan(randomGenerator.nextDouble(), randomConsumer);
	}
}
