package nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.PluginData;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_RunContinuityPluginData {

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RunContinuityPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "getCompletionCount", args = {})
	public void testGetCompletionCount() {
		for (int i = 0; i < 10; i++) {
			RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder().setCompletionCount(i)
					.build();
			assertEquals(i, runContinuityPluginData.getCompletionCount());
		}

	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "getConsumers", args = {})
	public void testGetConsumers() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5172593709525641500L);
		List<Pair<Double, Consumer<ActorContext>>> expectedPairs = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			Pair<Double, Consumer<ActorContext>> pair = new Pair<Double, Consumer<ActorContext>>(
					randomGenerator.nextDouble(), (c) -> {
					});
			expectedPairs.add(pair);
		}

		RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();//
		for (Pair<Double, Consumer<ActorContext>> pair : expectedPairs) {
			builder.addContextConsumer(pair.getFirst(), pair.getSecond());
		}
		RunContinuityPluginData runContinuityPluginData = builder.build();

		assertEquals(expectedPairs, runContinuityPluginData.getConsumers());

	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5885906221170851986L);

		for (int i = 0; i < 10; i++) {

			RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder()//
					.setCompletionCount(randomGenerator.nextInt(3))//
					.setPlansAreScheduled(randomGenerator.nextBoolean());//
			for (int j = 0; j < 3; j++) {
				builder.addContextConsumer(randomGenerator.nextDouble(), (c) -> {
				});
			}
			RunContinuityPluginData runContinuityPluginData = builder.build();

			RunContinuityPluginData cloneRunContinuityPluginData = //
					(RunContinuityPluginData) runContinuityPluginData.getCloneBuilder().build();
			
			assertEquals(
			runContinuityPluginData.getCompletionCount(),
			cloneRunContinuityPluginData.getCompletionCount());
			
			assertEquals(
			runContinuityPluginData.getConsumers(),
			cloneRunContinuityPluginData.getConsumers());
			
		}
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "plansAreScheduled", args = {})
	public void testPlansAreScheduled() {
		fail();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.class, name = "allPlansComplete", args = {})
	public void testAllPlansComplete() {
		fail();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		fail();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "setPlansAreScheduled", args = {
			boolean.class })
	public void testSetPlansAreScheduled() {
		fail();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "addContextConsumer", args = { double.class,
			Consumer.class })
	public void testAddContextConsumer() {
		fail();
	}

	@Test
	@UnitTestMethod(target = RunContinuityPluginData.Builder.class, name = "setCompletionCount", args = { int.class })
	public void testSetCompletionCount() {
		fail();
	}

}
