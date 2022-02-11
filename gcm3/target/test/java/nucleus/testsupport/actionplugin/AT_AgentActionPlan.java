package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AgentActionPlan.class)
public class AT_AgentActionPlan {
	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class })
	public void testConstructor() {
		AgentActionPlan agentActionPlan = new AgentActionPlan(0.0, (c) -> {
		});
		assertTrue(agentActionPlan.getKey().isPresent());
	}

	@Test
	@UnitTestConstructor(args = { double.class, Consumer.class, boolean.class })
	public void testConstructor_OptionalKey() {
		AgentActionPlan agentActionPlan = new AgentActionPlan(0.0, (c) -> {
		}, false);
		assertFalse(agentActionPlan.getKey().isPresent());

		agentActionPlan = new AgentActionPlan(0.0, (c) -> {
		}, true);
		assertTrue(agentActionPlan.getKey().isPresent());
	}

	/**
	 * Show that the agent action plan can be executed and will result in
	 * executed() returning true even if an exception is thrown in the plan.
	 */
	@Test
	@UnitTestMethod(name = "executed", args = {})
	public void testExecuted() {

		AgentActionPlan agentActionPlan = new AgentActionPlan(0.0, (c) -> {
		}, false);
		assertFalse(agentActionPlan.executed());
		agentActionPlan.executeAction(null);
		assertTrue(agentActionPlan.executed());

		AgentActionPlan agentActionPlanWithException = new AgentActionPlan(0.0, (c) -> {
			throw new RuntimeException();
		}, false);
		assertFalse(agentActionPlanWithException.executed());
		assertThrows(RuntimeException.class, () -> agentActionPlanWithException.executeAction(null));
		assertTrue(agentActionPlanWithException.executed());
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
		AgentActionPlan agentActionPlan = new AgentActionPlan(0.0, (c) -> {
		}, false);
		assertFalse(agentActionPlan.getKey().isPresent());

		/*
		 * Create a container to record the keys for the agent actions plans
		 * that will help us show that each key is unique
		 */
		Set<Object> keys = new LinkedHashSet<>();

		// use the constructor with explicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			agentActionPlan = new AgentActionPlan(0.0, (c) -> {
			}, true);
			assertTrue(agentActionPlan.getKey().isPresent());
			boolean unique = keys.add(agentActionPlan.getKey().get());
			assertTrue(unique);
		}

		// use the constructor with implicit inclusion of a key
		for (int i = 0; i < 30; i++) {
			agentActionPlan = new AgentActionPlan(0.0, (c) -> {
			});
			assertTrue(agentActionPlan.getKey().isPresent());
			boolean unique = keys.add(agentActionPlan.getKey().get());
			assertTrue(unique);
		}

	}

	@Test
	@UnitTestMethod(name = "getScheduledTime", args = {})
	public void testGetScheduledTime() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(918257164535899051L);

		// use the various constructors
		for (int i = 0; i < 300; i++) {
			double planTime = randomGenerator.nextDouble() * 1000;
			AgentActionPlan agentActionPlan = new AgentActionPlan(planTime, (c) -> {
			}, true);
			assertEquals(planTime, agentActionPlan.getScheduledTime());

			agentActionPlan = new AgentActionPlan(planTime, (c) -> {
			}, false);
			assertEquals(planTime, agentActionPlan.getScheduledTime());

			agentActionPlan = new AgentActionPlan(planTime, (c) -> {
			});
			assertEquals(planTime, agentActionPlan.getScheduledTime());
		}

	}

}
