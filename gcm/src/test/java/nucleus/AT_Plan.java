package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_Plan {

	class TestPlanData1 implements PlanData {
	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(Plan.builder(ActorContext.class).build());
	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "setTime", args = { double.class })
	public void testSetTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3070829833293509127L);

		for (int i = 0; i < 10; i++) {
			double time = randomGenerator.nextDouble() * 100;
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setTime(time).build();

			assertEquals(time, plan.getTime());
		}

	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "setActive", args = { boolean.class })
	public void testSetActive() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2020906151186894101L);

		for (int i = 0; i < 10; i++) {
			boolean active = randomGenerator.nextBoolean();
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setActive(active).build();

			assertEquals(active, plan.isActive());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertTrue(plan.isActive());
	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "setPlanData", args = { PlanData.class })
	public void testSetPlanData() {
		for (int i = 0; i < 10; i++) {
			TestPlanData1 testPlanData1 = new TestPlanData1();
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setPlanData(testPlanData1).build();

			assertEquals(testPlanData1, plan.getPlanData());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertEquals(null, plan.getPlanData());
	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "setKey", args = { Object.class })
	public void testSetKey() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2020906151186894101L);

		for (int i = 0; i < 10; i++) {
			String key = "TestKey" + randomGenerator.nextInt(100) + i % 2;
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setKey(key).build();

			assertEquals(key, plan.getKey());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertEquals(null, plan.getKey());
	}

	@Test
	@UnitTestMethod(target = Plan.Builder.class, name = "setCallbackConsumer", args = { Consumer.class })
	public void testSetCallbackConsumer() {
		for (int i = 0; i < 10; i++) {
			MutableBoolean called = new MutableBoolean(false);
			Consumer<ActorContext> callbackConsumer = (c) -> called.setValue(true);
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setCallbackConsumer(callbackConsumer).build();

			assertEquals(callbackConsumer, plan.getCallbackConsumer());

			plan.getCallbackConsumer().accept(null);

			assertTrue(called.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "builder", args = { Class.class })
	public void testBuilder() {
		assertNotNull(Plan.builder(ActorContext.class));
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "getCallbackConsumer", args = {})
	public void testGetCallbackConsumer() {
		for (int i = 0; i < 10; i++) {
			MutableBoolean called = new MutableBoolean(false);
			Consumer<ActorContext> callbackConsumer = (c) -> called.setValue(true);
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setCallbackConsumer(callbackConsumer).build();

			assertEquals(callbackConsumer, plan.getCallbackConsumer());

			plan.getCallbackConsumer().accept(null);

			assertTrue(called.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "getKey", args = {})
	public void testGetKey() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(544728351791286073L);

		for (int i = 0; i < 10; i++) {
			String key = "TestKey" + randomGenerator.nextInt(100) + i % 2;
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setKey(key).build();

			assertEquals(key, plan.getKey());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertEquals(null, plan.getKey());
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "getPlanData", args = {})
	public void testGetPlanData() {
		for (int i = 0; i < 10; i++) {
			TestPlanData1 testPlanData1 = new TestPlanData1();
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setPlanData(testPlanData1).build();

			assertEquals(testPlanData1, plan.getPlanData());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertEquals(null, plan.getPlanData());
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "getTime", args = {})
	public void testGetTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6688329851268154810L);

		for (int i = 0; i < 10; i++) {
			double time = randomGenerator.nextDouble() * 100;
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setTime(time).build();

			assertEquals(time, plan.getTime());
		}
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "isActive", args = {})
	public void testIsActive() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1531401382595524054L);

		for (int i = 0; i < 10; i++) {
			boolean active = randomGenerator.nextBoolean();
			Plan<ActorContext> plan = Plan.builder(ActorContext.class).setActive(active).build();

			assertEquals(active, plan.isActive());
		}

		Plan<ActorContext> plan = Plan.builder(ActorContext.class).build();

		assertTrue(plan.isActive());
	}

	@Test
	@UnitTestMethod(target = Plan.class, name = "toString", args = {})
	public void testToString() {
		// is essentially a debug capability and does not warrant a test since its
		// implementation is subject to a wide variety of implementations
	}
}
